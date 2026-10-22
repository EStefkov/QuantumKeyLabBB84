
from pathlib import Path

import pandas as pd
import matplotlib.pyplot as plt

CSV_FILE = Path("bb84_experiment_runs.csv")
OUTPUT_DIR = Path("bb84_graphs")
OUTPUT_DIR.mkdir(exist_ok=True)

df = pd.read_csv(CSV_FILE, header=0)

df.columns = [
    "N",
    "EveProbability",
    "NoiseProbability",
    "RunNumber",
    "Seed",
    "SiftedBits",
    "SiftedPercent",
    "TestBits",
    "TestErrors",
    "QBERPercent",
    "CandidateBits",
    "Decision",
    "DurationNs",
]

# Convert numeric columns safely.
numeric_columns = [
    "N", "EveProbability", "NoiseProbability",
    "RunNumber", "Seed", "SiftedBits", "SiftedPercent",
    "TestBits", "TestErrors", "QBERPercent",
    "CandidateBits", "DurationNs",
]

for column in numeric_columns:
    df[column] = pd.to_numeric(df[column], errors="coerce")

df["Decision"] = df["Decision"].astype(str).str.strip().str.lower()
df["DurationMs"] = df["DurationNs"] / 1_000_000

print(f"Total CSV records: {len(df)}")
print(f"Unique N values: {sorted(df['N'].dropna().unique())}")
print(f"Decision counts:\n{df['Decision'].value_counts(dropna=False)}")

# Save grouped statistics for later analysis.
summary = (
    df.groupby(["N", "EveProbability", "NoiseProbability"], dropna=False)
    .agg(
        Runs=("RunNumber", "count"),
        MeanQBER=("QBERPercent", "mean"),
        StdQBER=("QBERPercent", "std"),
        MeanSiftedPercent=("SiftedPercent", "mean"),
        AcceptCount=("Decision", lambda s: (s == "accept").sum()),
        RejectCount=("Decision", lambda s: (s == "reject").sum()),
        InsufficientCount=(
            "Decision", lambda s: (s == "insufficient_data").sum()
        ),
        MeanDurationMs=("DurationMs", "mean"),
        StdDurationMs=("DurationMs", "std"),
    )
    .reset_index()
)

summary.to_csv(
    OUTPUT_DIR / "bb84_summary.csv",
    index=False,
    na_rep="N/A",
)

# Graph 1: mean QBER by Eve probability.
# Each panel represents one N; each line represents a noise level.
fig, axes = plt.subplots(1, 3, figsize=(16, 5), sharey=True)

for ax, n in zip(axes, sorted(df["N"].dropna().unique())):
    subset = df[df["N"] == n]

    for noise in sorted(subset["NoiseProbability"].dropna().unique()):
        group = subset[subset["NoiseProbability"] == noise]
        stats = group.groupby("EveProbability")["QBERPercent"].mean()

        ax.plot(
            stats.index,
            stats.values,
            marker="o",
            label=f"Noise = {noise:.2f}",
        )

    ax.set_title(f"N = {int(n)}")
    ax.set_xlabel("Eve probability")
    ax.grid(True, alpha=0.3)
    ax.legend(fontsize=8)

axes[0].set_ylabel("Mean QBER (%)")
fig.suptitle("BB84: Mean QBER vs Eve probability")
fig.tight_layout()
fig.savefig(OUTPUT_DIR / "01_qber_vs_eve.png", dpi=180)
plt.close(fig)

# Graph 2: rejection rates by Eve probability.
# Denominator includes all runs in each configuration.
grouped = df.groupby(
    ["N", "EveProbability", "NoiseProbability"],
    dropna=False,
)

rates = grouped.apply(
    lambda g: pd.Series({
        "RejectRate": 100 * (g["Decision"] == "reject").sum() / len(g),
        "InsufficientRate": (
            100 * (g["Decision"] == "insufficient_data").sum() / len(g)
        ),
    }),
    include_groups=False,
).reset_index()

fig, axes = plt.subplots(1, 3, figsize=(16, 5), sharey=True)

for ax, n in zip(axes, sorted(df["N"].dropna().unique())):
    subset = rates[rates["N"] == n]

    for noise in sorted(subset["NoiseProbability"].dropna().unique()):
        group = subset[subset["NoiseProbability"] == noise]

        ax.plot(
            group["EveProbability"],
            group["RejectRate"],
            marker="o",
            label=f"Reject, noise={noise:.2f}",
        )

    ax.set_title(f"N = {int(n)}")
    ax.set_xlabel("Eve probability")
    ax.grid(True, alpha=0.3)
    ax.legend(fontsize=7)

axes[0].set_ylabel("Rejected runs (%)")
fig.suptitle("BB84: Rejection rate by Eve probability")
fig.tight_layout()
fig.savefig(OUTPUT_DIR / "02_rejection_rate.png", dpi=180)
plt.close(fig)

# Graph 3: mean execution time by N.
# All Eve/noise configurations are included in each N group.
runtime = (
    df.groupby("N")["DurationMs"]
    .agg(["mean", "std"])
    .reset_index()
)

plt.figure(figsize=(8, 5))
plt.errorbar(
    runtime["N"],
    runtime["mean"],
    yerr=runtime["std"].fillna(0),
    marker="o",
    capsize=5,
)
plt.xlabel("Number of transmitted bits (N)")
plt.ylabel("Execution time (ms)")
plt.title("BB84: Mean execution time by experiment size")
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig(OUTPUT_DIR / "03_execution_time.png", dpi=180)
plt.close()

print(f"\nGraphs and summary saved to: {OUTPUT_DIR.resolve()}")
print(summary.to_string(index=False))

import fs from 'node:fs/promises';
import path from 'node:path';
import { pathToFileURL } from 'node:url';
import { Presentation, PresentationFile } from '@oai/artifact-tool';

const workspaceDir = '/Users/estefkov1996/Desktop/cryptography/quantum_key_lab_bb84';
const skillDir = '/Users/estefkov1996/.codex/plugins/cache/openai-primary-runtime/presentations/26.1020.11127/skills/presentations';
const runtimePython = '/Users/estefkov1996/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3';
const finalPath = path.join(workspaceDir, 'output/Quantum_Key_Lab_BB84.pptx');
const stagingDir = path.join(workspaceDir, '.pptx_build/staging');
const renderDir = path.join(workspaceDir, '.pptx_build/render');
const candidatePath = path.join(stagingDir, 'candidate.pptx');
process.env.RUNTIME_NODE_MODULES = '/Users/estefkov1996/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules';
const C = { navy: '#12344A', teal: '#0B7A83', aqua: '#D9F0F0', paper: '#F7FAFC', white: '#FFFFFF', ink: '#163345', muted: '#587080', orange: '#E58B37', red: '#BA514B', line: '#B6CBD3', pale: '#EAF2F5' };
const FONT = 'Arial';

await fs.mkdir(stagingDir, { recursive: true });
await fs.mkdir(renderDir, { recursive: true });
await fs.mkdir(path.dirname(finalPath), { recursive: true });

const p = Presentation.create({ slideSize: { width: 1280, height: 720 } });

function shape(slide, geometry, name, x, y, w, h, fill = 'none', stroke = 'none', width = 0) {
  return slide.shapes.add({
    geometry, name,
    position: { left: x, top: y, width: w, height: h },
    fill,
    line: { style: 'solid', fill: stroke, width },
  });
}

function text(slide, name, value, x, y, w, h, style = {}) {
  const s = shape(slide, 'textbox', name, x, y, w, h);
  s.text = value;
  s.text.style = {
    typeface: FONT,
    fontSize: 24,
    color: C.ink,
    wrap: 'square',
    autoFit: 'shrinkText',
    verticalAlignment: 'top',
    insets: { top: 2, right: 2, bottom: 2, left: 2 },
    ...style,
  };
  return s;
}

function title(slide, heading, section = '') {
  text(slide, `title-${heading}`, heading, 64, 48, 1150, 58, { fontSize: 38, bold: true, color: C.navy, verticalAlignment: 'middle' });
  shape(slide, 'rect', 'title-accent', 64, 115, 74, 5, C.teal);
  if (section) text(slide, `section-${section}`, section.toUpperCase(), 148, 103, 600, 25, { fontSize: 16, bold: true, color: C.teal, verticalAlignment: 'middle' });
}

function footer(slide, n) {
  text(slide, `footer-${n}`, `QUANTUM KEY LAB   •   ${String(n).padStart(2, '0')}`, 930, 682, 290, 20, { fontSize: 12, color: C.muted, alignment: 'right', verticalAlignment: 'middle' });
}

function note(slide, value) { slide.speakerNotes.text = value; }

// 1. Cover
{
  const s = p.slides.add();
  s.background.fill = C.navy;
  shape(s, 'rect', 'cover-accent', 76, 120, 12, 405, C.teal);
  text(s, 'cover-kicker', 'КРИПТОГРАФИЯ  /  ПРОЕКТ 23', 112, 122, 720, 28, { fontSize: 18, bold: true, color: '#83D4D4', verticalAlignment: 'middle' });
  text(s, 'cover-title', 'Quantum Key Lab', 108, 205, 940, 84, { fontSize: 60, bold: true, color: C.white, verticalAlignment: 'middle' });
  text(s, 'cover-subtitle', 'Симулация на BB84 и засичане на подслушване', 112, 310, 950, 75, { fontSize: 34, color: '#DCEBF0', verticalAlignment: 'middle' });
  text(s, 'cover-bottom', 'Alice  •  Bob  •  Eve     |     Java симулация', 112, 470, 850, 35, { fontSize: 20, color: '#B9D0D8', verticalAlignment: 'middle' });
  note(s, 'Представете проекта като учебна симулация на BB84. Подчертайте, че резултатите са от класически софтуерен модел, а не от реално квантово устройство.');
}

// 2. Protocol flow
{
  const s = p.slides.add({ background: { fill: C.paper } });
  title(s, 'Роли и обмен на един сигнал', 'Протокол');
  const y = 218;
  const actors = [
    { x: 72, w: 260, name: 'Alice', detail: 'Избира бит и базис\nZ или X\n\nПодготвя състояние', color: C.teal },
    { x: 505, w: 260, name: 'Eve (по избор)', detail: 'Прихваща с вероятност f\nИзбира базис и измерва\nИзпраща ново състояние', color: C.orange },
    { x: 945, w: 260, name: 'Bob', detail: 'Избира базис независимо\nИзмерва състоянието\nЗаписва бит и базис', color: C.navy },
  ];
  for (const a of actors) {
    text(s, `actor-${a.name}`, a.name, a.x, y, a.w, 48, { fontSize: 30, bold: true, color: a.color, alignment: 'center', verticalAlignment: 'middle' });
    shape(s, 'rect', `line-${a.name}`, a.x + 35, y + 52, a.w - 70, 3, a.color);
    text(s, `detail-${a.name}`, a.detail, a.x + 8, y + 78, a.w - 16, 160, { fontSize: 23, color: C.ink, alignment: 'center', verticalAlignment: 'top' });
  }
  text(s, 'arrow-a-e', '→', 352, y + 82, 120, 70, { fontSize: 54, bold: true, color: C.muted, alignment: 'center', verticalAlignment: 'middle' });
  text(s, 'arrow-e-b', '→', 790, y + 82, 120, 70, { fontSize: 54, bold: true, color: C.muted, alignment: 'center', verticalAlignment: 'middle' });
  shape(s, 'rect', 'basis-note-bg', 90, 526, 1100, 74, C.aqua);
  text(s, 'basis-note', 'При различни базиси измерването е случаен бит.\nКвантова библиотека и хардуер не се използват.', 110, 537, 1060, 54, { fontSize: 21, color: C.navy, alignment: 'center', verticalAlignment: 'middle' });
  footer(s, 2);
  note(s, 'Обяснете ролите последователно отляво надясно. Важно е Eve да измери в избран от нея базис и да подготви ново състояние. В симулатора измерване в несъвпадащ базис дава случаен резултат. Източник: src/simulation/BB84Simulator.java.');
}

// 3. Sifting and QBER
{
  const s = p.slides.add({ background: { fill: C.white } });
  title(s, 'Пресяване и оценка на грешките', 'Правило за решение');
  const steps = [
    ['01', 'Сравнение на базиси', 'Запазват се само позициите,\nкъдето Alice и Bob съвпадат.'],
    ['02', 'Тестова извадка', 'Случайни 20% от пресетите\nбитове, закръглени нагоре.'],
    ['03', 'Изчисляване на QBER', 'Грешки в теста ÷ размер на теста\n× 100%.'],
  ];
  for (let i = 0; i < steps.length; i++) {
    const x = 70 + i * 404;
    text(s, `step-num-${i}`, steps[i][0], x, 180, 66, 42, { fontSize: 28, bold: true, color: C.teal, verticalAlignment: 'middle' });
    text(s, `step-title-${i}`, steps[i][1], x, 228, 350, 48, { fontSize: 25, bold: true, color: C.navy, verticalAlignment: 'middle' });
    text(s, `step-desc-${i}`, steps[i][2], x, 286, 350, 78, { fontSize: 20, color: C.muted });
    if (i < 2) text(s, `step-arrow-${i}`, '→', x + 342, 225, 52, 48, { fontSize: 36, bold: true, color: C.orange, alignment: 'center', verticalAlignment: 'middle' });
  }
  shape(s, 'rect', 'threshold-bg', 70, 414, 535, 144, C.navy);
  text(s, 'threshold-label', 'Учебен праг', 98, 432, 470, 35, { fontSize: 20, color: '#9EDADD', bold: true });
  text(s, 'threshold-value', 'QBER > 8%  →  отказ', 98, 471, 470, 46, { fontSize: 30, color: C.white, bold: true, verticalAlignment: 'middle' });
  shape(s, 'rect', 'candidate-bg', 640, 414, 560, 144, C.pale);
  text(s, 'candidate-label', 'Оставащ материал', 670, 432, 500, 35, { fontSize: 20, color: C.teal, bold: true });
  text(s, 'candidate-value', 'Candidate bits = sifted − test', 670, 471, 500, 46, { fontSize: 26, color: C.navy, bold: true, verticalAlignment: 'middle' });
  text(s, 'key-caveat', 'Остатъчните битове са кандидат-материал, не доказано сигурен ключ.', 98, 602, 1080, 35, { fontSize: 19, color: C.red, bold: true, alignment: 'center', verticalAlignment: 'middle' });
  footer(s, 3);
  note(s, 'Извадката се сравнява по класически канал и се изключва от кандидат-материала. QBER е изчислен само върху тестовите битове. При липса на извадка се отчита insufficient_data, а не нулева грешка. Проектът не прави корекция на грешки, privacy amplification или удостоверяване на класическия канал.');
}

// 4. Experiment design
{
  const s = p.slides.add({ background: { fill: C.paper } });
  title(s, 'План на експеримента', 'Параметри');
  const cols = [
    { x: 78, title: 'Размер N', values: '200\n1 000\n5 000', accent: C.teal },
    { x: 455, title: 'Вероятност за Eve f', values: '0\n0,25\n0,5\n1', accent: C.orange },
    { x: 832, title: 'Шум', values: '0\n0,01\n0,03', accent: C.navy },
  ];
  for (const c of cols) {
    text(s, `param-title-${c.title}`, c.title, c.x, 188, 330, 45, { fontSize: 25, bold: true, color: c.accent, verticalAlignment: 'middle' });
    shape(s, 'rect', `param-line-${c.title}`, c.x, 238, 310, 3, c.accent);
    text(s, `param-values-${c.title}`, c.values, c.x, 258, 310, 180, { fontSize: 25, color: C.ink, lineSpacing: 1.18 });
  }
  shape(s, 'rect', 'experiment-totals-bg', 78, 484, 1120, 105, C.navy);
  text(s, 'experiment-totals', '36 комбинации  ×  100 независими изпълнения  =  3 600 серии', 108, 503, 1060, 62, { fontSize: 28, bold: true, color: C.white, alignment: 'center', verticalAlignment: 'middle' });
  text(s, 'seed-note', 'Всеки run има уникален seed за възпроизводимост. Резултатите: bb84_experiment_runs.csv.', 90, 615, 1100, 28, { fontSize: 17, color: C.muted, alignment: 'center', verticalAlignment: 'middle' });
  footer(s, 4);
  note(s, 'Показаните стойности са трите масива от ExperimentRunner.java. Всички 36 комбинации изпълнени по 100 пъти. CSV съдържа seed за всеки ред.');
}

// 5. QBER findings
{
  const s = p.slides.add({ background: { fill: C.white } });
  title(s, 'QBER нараства с прихващането', 'Резултати');
  s.charts.add('line', {
    position: { left: 62, top: 164, width: 810, height: 370 },
    title: 'Среден QBER при N = 1 000',
    titleTextStyle: { fontSize: 18, fill: C.navy, bold: true },
    categories: ['0', '0,25', '0,5', '1'],
    series: [
      { name: 'Шум 0', values: [0.00, 6.43, 12.43, 25.03], line: { style: 'solid', fill: C.teal, width: 3 }, marker: { symbol: 'circle', size: 7, fill: C.teal } },
      { name: 'Шум 0,01', values: [0.93, 7.64, 13.32, 25.11], line: { style: 'solid', fill: C.orange, width: 3 }, marker: { symbol: 'circle', size: 7, fill: C.orange } },
      { name: 'Шум 0,03', values: [3.20, 8.82, 14.50, 26.63], line: { style: 'solid', fill: C.navy, width: 3 }, marker: { symbol: 'circle', size: 7, fill: C.navy } },
    ],
    hasLegend: true,
    legend: { position: 'bottom', overlay: false, textStyle: { fontSize: 15, fill: C.muted } },
    xAxis: { title: { text: 'Вероятност за прихващане f', textStyle: { fontSize: 16, fill: C.ink } }, textStyle: { fontSize: 15, fill: C.muted }, line: { style: 'solid', fill: C.line, width: 1 } },
    yAxis: { title: { text: 'Среден QBER (%)', textStyle: { fontSize: 16, fill: C.ink } }, min: 0, max: 30, majorUnit: 5, textStyle: { fontSize: 15, fill: C.muted }, majorGridlines: { style: 'solid', fill: C.pale, width: 1 } },
    chartFill: C.white,
    chartLine: { style: 'solid', fill: 'none', width: 0 },
    plotAreaFill: C.white,
    plotAreaLine: { style: 'solid', fill: 'none', width: 0 },
  });
  text(s, 'qber-result-head', 'При f = 1\nбез добавен шум', 905, 182, 300, 95, { fontSize: 24, bold: true, color: C.teal, alignment: 'center', verticalAlignment: 'middle' });
  text(s, 'qber-result-values', 'N=200   26,22%\nN=1 000 25,03%\nN=5 000 25,16%', 900, 294, 310, 155, { fontSize: 25, bold: true, color: C.navy, alignment: 'center', verticalAlignment: 'middle' });
  shape(s, 'rect', 'qber-callout-bg', 900, 474, 300, 92, C.aqua);
  text(s, 'qber-callout', 'Очакване при пълен\nintercept-resend: ≈25%', 917, 488, 266, 62, { fontSize: 20, color: C.navy, alignment: 'center', verticalAlignment: 'middle' });
  text(s, 'qber-caption', 'Средната грешка се доближава до 25%; при по-голям N вариацията намалява.', 80, 584, 1110, 38, { fontSize: 19, color: C.ink, alignment: 'center', verticalAlignment: 'middle' });
  footer(s, 5);
  note(s, 'Графиката е генерирана от 3 600-редовия CSV. При f=1 и noise=0 измереният среден QBER е 26,22% при N=200, 25,03% при N=1000 и 25,16% при N=5000. Стандартното отклонение намалява от 9,99 на 1,72 процентни пункта. Източник: bb84_experiment_runs.csv; bb84_graphs/01_qber_vs_eve.png.');
}

// 6. Decision outcomes and timing
{
  const s = p.slides.add({ background: { fill: C.paper } });
  title(s, 'Отказ и време за изпълнение', 'Резултати');
  s.charts.add('line', {
    position: { left: 54, top: 158, width: 820, height: 330 },
    title: 'Дял отказани серии при N = 1 000',
    titleTextStyle: { fontSize: 18, fill: C.navy, bold: true },
    categories: ['0', '0,25', '0,5', '1'],
    series: [
      { name: 'Шум 0', values: [0, 28, 88, 100], line: { style: 'solid', fill: C.teal, width: 3 }, marker: { symbol: 'circle', size: 7, fill: C.teal } },
      { name: 'Шум 0,01', values: [0, 42, 97, 100], line: { style: 'solid', fill: C.orange, width: 3 }, marker: { symbol: 'circle', size: 7, fill: C.orange } },
      { name: 'Шум 0,03', values: [3, 60, 97, 100], line: { style: 'solid', fill: C.navy, width: 3 }, marker: { symbol: 'circle', size: 7, fill: C.navy } },
    ],
    hasLegend: true,
    legend: { position: 'bottom', overlay: false, textStyle: { fontSize: 14, fill: C.muted } },
    xAxis: { title: { text: 'Вероятност за прихващане f', textStyle: { fontSize: 16, fill: C.ink } }, textStyle: { fontSize: 15, fill: C.muted }, line: { style: 'solid', fill: C.line, width: 1 } },
    yAxis: { title: { text: 'Отказани серии (%)', textStyle: { fontSize: 16, fill: C.ink } }, min: 0, max: 100, majorUnit: 20, textStyle: { fontSize: 15, fill: C.muted }, majorGridlines: { style: 'solid', fill: C.pale, width: 1 } },
    chartFill: C.paper,
    chartLine: { style: 'solid', fill: 'none', width: 0 },
    plotAreaFill: C.paper,
    plotAreaLine: { style: 'solid', fill: 'none', width: 0 },
  });
  text(s, 'reject-insights', 'При f=1, noise=0\n\nN=200: 98 от 100 отказани\nN=1 000: 100 от 100\nN=5 000: 100 от 100\n\nПри f=0, noise=0\n0 фалшиви отказа във всички N', 900, 165, 300, 300, { fontSize: 19, color: C.ink, lineSpacing: 1.12 });
  shape(s, 'rect', 'timing-bg', 82, 520, 1110, 95, C.white);
  text(s, 'timing-copy', 'Средно измерено време на run: 0,027 ms (N=200)  •  0,262 ms (N=1 000)  •  0,288 ms (N=5 000)', 102, 536, 1070, 31, { fontSize: 19, bold: true, color: C.navy, alignment: 'center', verticalAlignment: 'middle' });
  text(s, 'timing-caveat', 'Измерванията са ориентировъчни: текущият runner няма отделни загряващи извиквания.', 105, 575, 1060, 25, { fontSize: 16, color: C.red, alignment: 'center', verticalAlignment: 'middle' });
  footer(s, 6);
  note(s, 'Графиката показва процент на отказ от всички 100 серии във всяка конфигурация. При чист честен канал няма откази. Само шумът може да създаде фалшив отказ: при N=1000 и noise=0.03 са отказани 3 от 100 серии. Таймерът е System.nanoTime, но не са използвани отделни загряващи серии; описвайте резултатите като ориентировъчни, а не като прецизен benchmark.');
}

// 7. Tests and boundary cases
{
  const s = p.slides.add({ background: { fill: C.white } });
  title(s, 'Тестове и гранични случаи', 'Проверки');
  text(s, 'test-count', '18', 82, 185, 165, 110, { fontSize: 76, bold: true, color: C.teal, alignment: 'center', verticalAlignment: 'middle' });
  text(s, 'test-label', 'автоматизирани проверки\nуспешно преминали', 250, 201, 340, 68, { fontSize: 23, color: C.navy, verticalAlignment: 'middle' });
  shape(s, 'rect', 'test-divider', 82, 318, 1100, 2, C.line);
  text(s, 'edge-one-label', 'ГРАНИЧЕН СЛУЧАЙ 01', 86, 348, 365, 26, { fontSize: 16, bold: true, color: C.orange });
  text(s, 'edge-one', 'Шум = 1\nВсички тестови битове са обърнати → QBER = 100% → reject.', 86, 382, 500, 90, { fontSize: 21, color: C.ink });
  text(s, 'edge-two-label', 'ГРАНИЧЕН СЛУЧАЙ 02', 648, 348, 365, 26, { fontSize: 16, bold: true, color: C.orange });
  text(s, 'edge-two', 'Няма съвпаднали базиси\nТестовата извадка е празна → insufficient_data; QBER е N/A.', 648, 382, 500, 90, { fontSize: 21, color: C.ink });
  shape(s, 'rect', 'test-journal-bg', 85, 520, 1100, 83, C.pale);
  text(s, 'test-journal', 'Проверени са още: N=0, вероятности извън [0,1], еднакъв seed, пресяване и изключване на тестовите позиции. Журналът се създава при изпълнение: test_journal.csv.', 108, 533, 1054, 56, { fontSize: 18, color: C.navy, verticalAlignment: 'middle' });
  footer(s, 7);
  note(s, 'Отворете test_journal.csv. За демонстрация първо стартирайте тестовете. Покажете T16 (noise probability 1, всички тестови битове са грешки и решението е reject) и T17 (един сигнал без съвпаднали базиси води до insufficient_data и NaN вътрешно). В CSV стойността за празен тест се записва като NaN; бележката в презентацията използва N/A като смисъл на неприложима QBER. Не твърдете, че GUI има тези контроли: тестовете извикват симулатора програмно.');
}

// 8. Demo plan and current limits
{
  const s = p.slides.add({ background: { fill: C.navy } });
  text(s, 'demo-title', 'Демонстрация и следваща стъпка', 66, 46, 1140, 58, { fontSize: 37, bold: true, color: C.white, verticalAlignment: 'middle' });
  shape(s, 'rect', 'demo-accent', 66, 116, 72, 5, '#83D4D4');
  text(s, 'demo-run-title', 'Сценарий за защита', 76, 160, 490, 38, { fontSize: 24, bold: true, color: '#83D4D4' });
  text(s, 'demo-steps', '1. Стартирайте пакетния експеримент:\n   java -cp out Main\n2. Покажете CSV с N, f, шум, QBER, решение и seed.\n3. Стартирайте проверките:\n   java -cp out BB84SimulatorTest\n4. Отворете test_journal.csv и обяснете T16 и T17.', 76, 210, 690, 242, { fontSize: 21, color: C.white, lineSpacing: 1.12 });
  shape(s, 'rect', 'demo-caveat-bg', 810, 172, 382, 272, '#1C4A5E');
  text(s, 'demo-caveat-head', 'Текущо ограничение', 835, 194, 330, 38, { fontSize: 22, bold: true, color: '#83D4D4' });
  text(s, 'demo-caveat', 'Версията е командно-редова. Липсва визуален изглед N=20 със стъпки за Alice, Eve и Bob.\n\nКандидат-битовете не са краен сигурен ключ.', 835, 245, 330, 160, { fontSize: 20, color: C.white });
  text(s, 'demo-closing', 'Извод: симулацията показва как прихващането и шумът влияят на QBER и на учебното решение за отказ.', 82, 516, 1095, 72, { fontSize: 25, bold: true, color: C.white, alignment: 'center', verticalAlignment: 'middle' });
  text(s, 'demo-source', 'Данни: bb84_experiment_runs.csv   |   Код: src/simulation, src/experiments', 90, 632, 1090, 26, { fontSize: 15, color: '#B9D0D8', alignment: 'center', verticalAlignment: 'middle' });
  footer(s, 8);
  note(s, 'Практически ред: ако вече е компилирано, изпълнете java -cp out Main. Това генерира 3600 run-а и презаписва CSV. След това изпълнете java -cp out BB84SimulatorTest. Покажете в журнала T16 и T17. За пълноценна защита на интерфейсната част все още трябва да бъде добавен интерактивен N=20 изглед; не представяйте командния ред като GUI.');
}

// Render every slide for inspection, then export a private draft PPTX.
for (const [index, slide] of p.slides.items.entries()) {
  const blob = await p.export({ slide, format: 'png', scale: 1 });
  await fs.writeFile(path.join(renderDir, `slide-${String(index + 1).padStart(2, '0')}.png`), new Uint8Array(await blob.arrayBuffer()));
}
await (await PresentationFile.exportPptx(p)).save(candidatePath);

const { finalizePresentation } = await import(pathToFileURL(path.join(skillDir, 'container_tools/artifact_tool_utils.mjs')).href);
const requirements = {
  explicitTotalSlideCount: 8,
  requiredNativeTableOwnerSlides: [],
  requiredNativeChartOwnerSlides: [5, 6],
  materializeLiteralChartWorkbooks: true,
};
const result = await finalizePresentation({
  ...requirements,
  workspaceDir,
  candidatePath,
  finalPath,
  pythonExecutable: runtimePython,
  integrityValidatorPath: path.join(skillDir, 'container_tools/inspect_presentation_package_integrity.py'),
  layoutValidatorPath: path.join(skillDir, 'container_tools/inspect_presentation_layout_geometry.py'),
  layoutArgs: ['--expected-slide-size-emu', '12192000,6858000', '--validate-bullet-geometry', '--validate-heading-fit'],
  fontPolicy: { basis: 'design', families: [FONT, 'Aptos'], scriptFonts: { cs: FONT } },
  verifyArtifactToolImport: true,
  receiptPath: path.join(stagingDir, 'Quantum_Key_Lab_BB84.validation-v5.json'),
});
console.log(JSON.stringify({ finalPath, result }, null, 2));

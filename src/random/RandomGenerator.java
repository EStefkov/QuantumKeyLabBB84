package random;

import java.sql.Timestamp;

public class RandomGenerator {


    private Timestamp seed;

    public RandomGenerator(Timestamp seed) {
        //Използваме сегашното време за генериране на случайно число
        this.seed = new Timestamp(System.currentTimeMillis());
    }

    public long generateRandomNum(){

        int year = seed.getYear();
        int month = seed.getMonth();
        int day = seed.getDay();
        int minute = seed.getMinutes();
        int second = seed.getSeconds();

        // Това ще е допълнително рандом число за да сме сигурни, че генерираме различни числа
        int randomNum = (int) (Math.random() * 100);
        randomNum = (year +month+second+minute+day)/5 + randomNum / 4;

        return randomNum;
    }

    // Генерира бит: 0 или 1
    public int generateBit() {
        return (int) (generateRandomNum() % 2);
    }

}

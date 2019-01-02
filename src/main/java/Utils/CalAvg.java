package Utils;

import java.util.Scanner;

public class CalAvg {

    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);
        double sum = 0;
        int count = 0;
        double temp;
        while ((temp = sc.nextDouble()) >0) {
            sum += temp;
            count++;
        }
        System.out.println(sum / count);
    }
}

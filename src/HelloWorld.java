import java.util.Scanner;

public class HelloWorld {
    public static void main(String[] args) {

//        int a1 = 4;
//        int b1 = 5;
//        System.out.println(a1 & b1);
//
//        int number = 45;
//        int key = 102;
//        int encrypt = number ^ key;
//        System.out.println("Зашифрованное число: " + encrypt);
//
//        int decrypt = encrypt ^ key;
//        System.out.println("Расшифрованное число: " + decrypt);

//        int value1 = 3;
//        int value2 = 2;
//        int value3 = 1;
//        int result = 0b0000_0000;
//
//        result = result | value1;
//        result = result << 2;
//        result = result | value2;
//        result = result << 2;
//        result = result | value3;
//
//        System.out.println(result);
//
//        int comeBack = 0b0000_0011;
//        int newValue3 = result & comeBack;
//        result = result >> 2;
//        int newValue2 = result & comeBack;
//        result = result >> 2;
//        int newValue1 = result & comeBack;
//        System.out.println(newValue1);
//        System.out.println(newValue2);
//        System.out.println(newValue3);

//        int[] array = new int[] {1, 2, 3, 4, 5};
//
//        for (int i = 0; i < array.length; i++) {
//            System.out.println(array[i]);
//        }

//        int[] array2 = new int[] {6, 7, 8, 9, 10};
//
//        for (int i : array2) {
//            System.out.println(i);
//        }

//        int[] array = {1, 2, 3, 4, 5};
//        for (int i = 0; i < array.length; i++) {
//            array[i] = i * 2;
//            System.out.println(array[i]);
//        }

//        int[][] nums =
//                {
//                        {1, 2, 3},
//                        {4, 5, 6},
//                        {7, 8, 9}
//                };
//        for (int i = 0; i < nums.length; i++) {
//            for (int j = 0; j < nums[i].length; j++) {
//                System.out.printf("%d", nums[i][j]);
//            }
//            System.out.println();
//        }

//        int[][] nums = new int[3][];
//        nums[0] = new int[2];
//        nums[1] = new int[3];
//        nums[2] = new int[5];
//
//        for (int i = 0; i < nums.length; i++) {
//            for (int j = 0; j < nums[i].length; j++) {
//                nums[i][j] = i + j;
//                System.out.printf("%d", nums[i][j]);
//            }
//            System.out.println();
//        }

//        String name = "Alex";
//
//        switch (name) {
//            case "Tom":
//                System.out.println("Your name - Tom");
//                break;
//            case "Bob":
//                System.out.println("Your name - Bob");
//                break;
//            case "Sam":
//                System.out.println("Your name - Sam");
//                break;
//            default:
//                System.out.println("Unknow name");
//                break;
//        }

        String name = "Tom";

        switch (name) {
            case "Bob" -> System.out.println("Your name - Bob");
            case "Tom" -> System.out.println("Your name - Tom");
            case "Sam" -> System.out.println("Your name Sam");
            default -> System.out.println("Unknow name");
        }
    }
}

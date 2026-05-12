// 导入Scanner类，用于接收控制台输入
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 创建Scanner对象，用于读取输入
        Scanner scanner = new Scanner(System.in);
        
        // 提示用户输入第一个整数
        int num1 = scanner.nextInt();  // 读取第一个整数
        
        // 提示用户输入第二个整数
        int num2 = scanner.nextInt();  // 读取第二个整数
        
        // 计算两个数的和
        int sum = num1 + num2;
        
        // 打印结果
        System.out.println(sum);
        
        // 关闭Scanner
        scanner.close();
    }
}
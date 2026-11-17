//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        Logger logger = new Logger();

        System.out.println(logger.shouldPrintMessage(1, "foo"));   // true
        System.out.println(logger.shouldPrintMessage(2, "bar"));   // true
        System.out.println(logger.shouldPrintMessage(3, "foo"));   // false
        System.out.println(logger.shouldPrintMessage(11, "foo"));  // true
        System.out.println(logger.shouldPrintMessage(12, "bar"));
    }
}
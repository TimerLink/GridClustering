package cn.edu.hit.gpcs.area.util;

/**
 * 控制台工具
 */
public class ConsoleUtils {
    /**
     * 控制台打印颜色
     */
    @SuppressWarnings("unused")
    public enum Color {
        ANSI_RESET  ("\u001B[0m"),
        ANSI_BLACK  ("\u001B[30m"),
        ANSI_RED    ("\u001B[31m"),
        ANSI_GREEN  ("\u001B[32m"),
        ANSI_YELLOW ("\u001B[33m"),
        ANSI_BLUE   ("\u001B[34m"),
        ANSI_PURPLE ("\u001B[35m"),
        ANSI_CYAN   ("\u001B[36m"),
        ANSI_WHITE  ("\u001B[37m");

        private String color;

        Color (String color) {
            this.color = color;
        }

        public String getColor () {
            return color;
        }
    }

    public static void print (Color color, String string) {
        System.out.print(color.getColor() + string + Color.ANSI_RESET.getColor());
    }

}

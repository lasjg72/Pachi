//package Monkey;

public class Main {
    public static void main(String[] args) {
        Spec_Monkey Monkey = new Spec_Monkey();
        Monkey.NB_init();
        Monkey.DB_init();
        Monkey.Calc_dedama_per_kakuhen();
        System.out.println("ラウンドベース込みの時給: " + Values.Calc_money_per_hour(Monkey)*4);
    }
}
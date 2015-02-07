package fyp.leungww.exsplit;


public class Account {
    private long _id;
    private String currency;
    private double balance;
    private long holder;

    public Account(long _id, long holder, String currency, double balance){
        this._id = _id;
        this.holder = holder;
        this.currency = currency;
        this.balance = balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public long get_id() {
        return _id;
    }

    public String getCurrency() {

        return currency;
    }

    public double getBalance() {
        return balance;
    }

    public long getHolder() {
        return holder;
    }
}

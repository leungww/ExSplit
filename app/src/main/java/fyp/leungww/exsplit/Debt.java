package fyp.leungww.exsplit;

public class Debt {
    private long _id;
    private double amountOwed;

    public Debt(long _id, double amountOwed){
        this._id = _id;
        this.amountOwed = amountOwed;
    }

    public void setAmountOwed(double amountOwed){
        this.amountOwed = amountOwed;
    }

    public long get_id() {
        return _id;
    }

    public double getAmountOwed() {
        return amountOwed;
    }
}

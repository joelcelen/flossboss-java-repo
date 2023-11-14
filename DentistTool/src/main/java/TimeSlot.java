public class TimeSlot {
    private String bookingStatus;
    private boolean available;

    public TimeSlot(String status, boolean available) {
        this.bookingStatus = status;
        this.available = available;
    }

    public String getStatus() {
        return bookingStatus;
    }

    public void setStatus(String status) {
        this.bookingStatus = status;
    }

    public boolean getAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}

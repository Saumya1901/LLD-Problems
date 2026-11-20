import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class MeetingRoomBookingSystem {
    Map<String, Room> getRoomById = new ConcurrentHashMap<>();

    BookingService bookingService = new BookingService(getRoomById);

    MeetingRoomBookingSystem() {

    }


    public List<Room> getRooms(int capacity, LocalDateTime startTime, LocalDateTime endTime) {
        List<Room> avlRooms = new ArrayList<>();
        for (Room room : getRoomById.values()) {
            synchronized (room) {
                if (room.getCapacity() >= capacity && room.canBook(startTime, endTime)) {
                    avlRooms.add(room);
                }

            }

        }
        return avlRooms;
    }

    public boolean bookRoom(Room room, LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) return false;
        return bookingService.bookRoom(room, startTime, endTime);
    }

    public void addRoom(int capacity) {
        Room room = new Room(capacity);
        getRoomById.computeIfAbsent(room.getRoomId(), k -> room);

    }

    boolean cancelBooking(String bookingId) {
        return bookingService.cancelBooking(bookingId);
    }


}

class Room {
    private final String roomId;
    private final int capacity;
    private final TreeMap<LocalDateTime, Booking> bookings;//startTime and booking

    public Room(int capacity) {
        this.roomId = UUID.randomUUID().toString();
        this.capacity = capacity;
        this.bookings = new TreeMap<>();
    }

    public void addBooking(LocalDateTime startTime, Booking booking) {
        bookings.put(startTime, booking);
    }

    public Booking getPrevBooking(LocalDateTime time) {
        Map.Entry<LocalDateTime, Booking> booking = bookings.floorEntry(time);

        return booking == null ? null : booking.getValue();
    }

    public Booking getNextBooking(LocalDateTime time) {
        Map.Entry<LocalDateTime, Booking> booking = bookings.ceilingEntry(time);
        return booking == null ? null : booking.getValue();
    }

    public String getRoomId() {
        return roomId;
    }

    public int getCapacity() {
        return capacity;
    }

    public TreeMap<LocalDateTime, Booking> getBookings() {
        return bookings;
    }

    public boolean canBook(LocalDateTime startTime, LocalDateTime endTime) {
        Booking prev = getPrevBooking(startTime);
        Booking next = getNextBooking(startTime);
        if (prev != null && prev.getEndTime().isAfter(startTime)) return false;
        if (next != null && next.getStartTime().isBefore(endTime)) return false;
        return true;

    }

    public void cancelBooking(LocalDateTime startTime, Booking booking) {
        bookings.remove(startTime, booking);
    }

    public boolean hasBooking(LocalDateTime startTime, LocalDateTime endTime) {
        if (bookings.containsKey(startTime) && bookings.get(startTime).getEndTime().isEqual(endTime)) return true;
        return false;
    }
}

class Booking {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String bookingId;
    private final String roomId;

    public Booking(LocalDateTime startTime, LocalDateTime endTime, String roomId) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomId = roomId;
        this.bookingId = UUID.randomUUID().toString();
    }

    public String getRoomId() {
        return roomId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getBookingId() {
        return bookingId;
    }

}

class BookingService {
    Map<String, Booking> bookingById = new HashMap<>();
    Map<String, Room> getRoomById;

    public BookingService(Map<String, Room> getRoomById) {
        this.getRoomById = getRoomById;
    }

    public boolean bookRoom(Room room, LocalDateTime startTime, LocalDateTime endTime) {

        synchronized (room) {
            if (!room.canBook(startTime, endTime)) {
                return false;
            }
            Booking booking = new Booking(startTime, endTime, room.getRoomId());
            room.addBooking(startTime, booking);
            bookingById.computeIfAbsent(booking.getBookingId(), k -> booking);
        }
        return true;
    }

    public boolean cancelBooking(String bookingId) {
        if (!bookingById.containsKey(bookingId)) return false;
        Room room = getRoomById.get(bookingById.get(bookingId).getRoomId());
        synchronized (room) {
            if (!bookingById.containsKey(bookingId)) return false;
            Booking booking = bookingById.get(bookingId);
            if (booking != null && room.hasBooking(booking.getStartTime(), booking.getEndTime())) {
                //we can cancel the booking
                bookingById.remove(bookingId, booking);
                room.cancelBooking(booking.getStartTime(), booking);
                return true;
            }
            return false;
        }


    }
}

class User {
    private final String userId;

    public User() {
        this.userId = UUID.randomUUID().toString();
    }
}

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.


    }
}
/*

MeetingRoomBookingSystem -> singletonPattern Facade
    ->(add Rooms to the system)
    -> find available rooms


    -> BookingService -> uses rooms and books the room
            (create Booking)
            (cancel Booking)

* */
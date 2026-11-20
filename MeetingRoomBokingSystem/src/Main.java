import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


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

//    public TreeMap<LocalDateTime, Booking> getBookings() {
//        return new TreeMap<>(bookings);
//    }//removed the defensive copy as someone can modify it

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

    public boolean hasBooking(Booking booking) {
        Booking existing = bookings.get(booking.getStartTime());

        return existing != null &&
                existing.getBookingId().equals(booking.getBookingId());
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
    Map<String, Booking> bookingById = new ConcurrentHashMap<>();
    Map<String, Room> getRoomById = new ConcurrentHashMap<>();

    public BookingService() {
    }

    public String addRoom(int capacity) {
        Room room = new Room(capacity);
        getRoomById.computeIfAbsent(room.getRoomId(), k -> room);
        return room.getRoomId();

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
    public String bookRoom(String roomId, LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) return null;
        Room room = getRoomById.get(roomId);
        if(room == null)return null;
        synchronized (room) {
            if (!room.canBook(startTime, endTime)) {
                return null;
            }
            Booking booking = new Booking(startTime, endTime, room.getRoomId());
            room.addBooking(startTime, booking);
            bookingById.computeIfAbsent(booking.getBookingId(), k -> booking);
            return booking.getBookingId();

        }
    }

    public boolean cancelBooking(String bookingId) {
        if (!bookingById.containsKey(bookingId)) return false;
        Booking booking1 = bookingById.get(bookingId);
        if(booking1 == null)return false;
        Room room = getRoomById.get(booking1.getRoomId());
        if(room == null)return false;

        synchronized (room) {
            Booking booking = bookingById.get(bookingId);
            if(booking!=booking1)return false;
            if (room.hasBooking(booking)) {
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

public class Main {
    public static void main(String[] args) {
        BookingService service = new BookingService();

        String roomId = service.addRoom(5);

        LocalDateTime ten = LocalDateTime.of(2026, 11, 21, 10, 0);
        LocalDateTime eleven = ten.plusHours(1);
        LocalDateTime twelve = ten.plusHours(2);

        String b1 = service.bookRoom(roomId, ten, eleven);
        System.out.println("First booking: " + (b1 != null));

        String overlap = service.bookRoom(
                roomId, ten.plusMinutes(30), eleven.plusMinutes(30)
        );
        System.out.println("Overlap rejected: " + (overlap == null));

        String b2 = service.bookRoom(roomId, eleven, twelve);
        System.out.println("Adjacent booking: " + (b2 != null));

        System.out.println("First cancellation: " + service.cancelBooking(b1));
        System.out.println("Duplicate cancellation: " + service.cancelBooking(b1));

        String b3 = service.bookRoom(roomId, ten, eleven);
        System.out.println("Rebooking: " + (b3 != null));
        System.out.println("Old cancellation rejected: " + !service.cancelBooking(b1));
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
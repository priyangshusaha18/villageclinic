import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

public class Main {

    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    private void registerPatient() {
        try (Connection con = DBConnection.getConnection()) {
            System.out.print("Enter patient's name: ");
            String name = br.readLine();

            System.out.print("Enter patient's age: ");
            int age = Integer.parseInt(br.readLine());

            System.out.print("Enter patient's village: ");
            String village = br.readLine();

            System.out.print("Enter patient's phone: ");
            String phone = br.readLine();

            String query = "INSERT INTO patients (name, age, village, phone) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, village);
            ps.setString(4, phone);

            ps.executeUpdate();
            System.out.println("\nPatient registered successfully.\n");

        } catch (Exception e) {
            System.out.println("\nUnable to register patient.");
            e.printStackTrace();
        }
    }

    private void recordVisit() {
        try (Connection con = DBConnection.getConnection()) {
            System.out.print("Enter patient's ID: ");
            int pat_id = Integer.parseInt(br.readLine());

            System.out.print("Enter diagnosis: ");
            String diagnosis = br.readLine();

            System.out.print("Enter medicine: ");
            String medicine = br.readLine();

            System.out.print("Enter follow-up date (YYYY-MM-DD) or leave blank: ");
            String date = br.readLine();

            String query = "INSERT INTO visits (pat_id, diagnosis, medicine, followup) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, pat_id);
            ps.setString(2, diagnosis);
            ps.setString(3, medicine);

            if (date == null || date.trim().isEmpty()) {
                ps.setNull(4, Types.DATE);
            } else {
                java.sql.Date followup = java.sql.Date.valueOf(date);
                ps.setDate(4, followup);
            }

            ps.executeUpdate();
            System.out.println("\nVisit recorded successfully.\n");

        } catch (Exception e) {
            System.out.println("\nUnable to record visit.");
            e.printStackTrace();
        }
    }

    private void viewPatientHistory() {
        try (Connection con = DBConnection.getConnection()) {
            System.out.print("Enter patient's ID: ");
            int pat_id = Integer.parseInt(br.readLine());

            String query = "SELECT * FROM visits WHERE pat_id = ? ORDER BY visit_date";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, pat_id);

            ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("\nNo visit history found for this patient.\n");
                return;
            }

            System.out.println("\n===== Patient Visit History =====");
            while (rs.next()) {
                System.out.println("Visit Date   : " + rs.getDate("visit_date"));
                System.out.println("Diagnosis    : " + rs.getString("diagnosis"));
                System.out.println("Medicine     : " + rs.getString("medicine"));
                System.out.println("Follow-up    : " + rs.getDate("followup"));
                System.out.println("------------------------------");
            }
            System.out.println();

        } catch (Exception e) {
            System.out.println("\nUnable to view patient history.");
            e.printStackTrace();
        }
    }

    private void followupDue() {
        try (Connection con = DBConnection.getConnection()) {

            String query = "SELECT patients.name, visits.diagnosis, visits.followup " +
                           "FROM patients JOIN visits ON patients.pat_id = visits.pat_id " +
                           "WHERE visits.followup <= CURDATE() AND visits.followup IS NOT NULL";

            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

            System.out.println("\nFollow-up Due:");
            while (rs.next()) {
                found = true;
                java.sql.Date followup = rs.getDate("followup");

                if (followup.before(today)) {
                    System.out.println(rs.getString("name") + " | " +
                                       rs.getString("diagnosis") + " | Due: " +
                                       followup + " [OVERDUE]");
                } else {
                    System.out.println(rs.getString("name") + " | " +
                                       rs.getString("diagnosis") + " | Due: " +
                                       followup);
                }
            }

            if (!found) {
                System.out.println("No follow-ups due today.");
            }
            System.out.println();

        } catch (Exception e) {
            System.out.println("\nUnable to fetch follow-up data.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Main obj = new Main();

        while (true) {
            try {
                System.out.println("===== Village Clinic System =====");
                System.out.println("1. Register Patient");
                System.out.println("2. Record Visit");
                System.out.println("3. View Patient History");
                System.out.println("4. Follow-up Due Today");
                System.out.println("5. Exit");
                System.out.print("Enter choice: ");

                int ch = Integer.parseInt(br.readLine());

                switch (ch) {
                    case 1:
                        obj.registerPatient();
                        break;
                    case 2:
                        obj.recordVisit();
                        break;
                    case 3:
                        obj.viewPatientHistory();
                        break;
                    case 4:
                        obj.followupDue();
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid option! Try again.\n");
                }

            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.\n");
            }
        }
    }
}
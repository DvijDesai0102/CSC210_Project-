import java.util.*;

/*
  ------------------------------------------------------------
   FinalRouteProject.java
  ------------------------------------------------------------
   Public Transport Route Finder Program
   (Bus + Train System)

   Features:
   ------------------------------------------------------------
   ▶ Bus Network: (UNCHANGED)
        - 15 stops, 5 buses
        - 7 minutes added for each bus transfer
        - Average speed: 1 km = 2 minutes
        - Fare Calculation:
            • First 5 km = ₹15
            • +₹5 for every additional 5 km
        - Displays:
            • All possible routes
            • Distance, Transfers, Time, Fare
            • Best (shortest and fastest) route

   ▶ Train Network: (UPDATED)
        - 8 stations (1–8)
        - Shows forward and backward directions
        - User selects start & end
        - 4 minutes per stop
        - Train arrives every 9 minutes → ETA shown
        - Fare:
            • 1–3 stops → ₹15
            • 4–5 stops → ₹20
            • 6–8 stops → ₹25
  ------------------------------------------------------------
*/

// ---------------- TRAIN LINKED LIST (UPDATED) ----------------
class DNode {
    int id;
    DNode prev, next;

    DNode(int id) {
        this.id = id;
    }
}

// ---------------- BUS GRAPH STRUCTURES ----------------
class Edge {
    int to;
    double distKm;
    int busId;

    Edge(int to, double distKm, int busId) {
        this.to = to;
        this.distKm = distKm;
        this.busId = busId;
    }
}

class RouteInfo {
    List<Integer> stations;
    double distanceKm;
    int transfers;
    double timeMin;
    double fare;

    RouteInfo(List<Integer> stations, double distanceKm, int transfers, double timeMin, double fare) {
        this.stations = stations;
        this.distanceKm = distanceKm;
        this.transfers = transfers;
        this.timeMin = timeMin;
        this.fare = fare;
    }
}

// ---------------- MAIN CLASS ----------------
public class FinalRouteProject {

    static Map<Integer, List<Edge>> graph = new HashMap<>();

    // ---------- TRAIN FUNCTIONS (UPDATED FULLY) ----------
    static DNode buildTrainLine() {
        DNode s1 = new DNode(1);
        DNode s2 = new DNode(2);
        DNode s3 = new DNode(3);
        DNode s4 = new DNode(4);
        DNode s5 = new DNode(5);
        DNode s6 = new DNode(6);
        DNode s7 = new DNode(7);
        DNode s8 = new DNode(8);

        s1.next = s2; s2.prev = s1;
        s2.next = s3; s3.prev = s2;
        s3.next = s4; s4.prev = s3;
        s4.next = s5; s5.prev = s4;
        s5.next = s6; s6.prev = s5;
        s6.next = s7; s7.prev = s6;
        s7.next = s8; s8.prev = s7;

        return s1;
    }

    static void printTrainForward(DNode head) {
        System.out.println("\nTrain Line (Forward):");
        for (DNode cur = head; cur != null; cur = cur.next)
            System.out.print(cur.id + (cur.next != null ? " -> " : ""));
        System.out.println();
    }

    static void printTrainBackward(DNode tail) {
        System.out.println("\nTrain Line (Backward):");
        for (DNode cur = tail; cur != null; cur = cur.prev)
            System.out.print(cur.id + (cur.prev != null ? " -> " : ""));
        System.out.println();
    }

    static int calculateTrainFare(int stops) {
        if (stops <= 3) return 15;
        if (stops <= 5) return 20;
        return 25;
    }

    // ---------- BUS GRAPH FUNCTIONS (UNCHANGED) ----------
    static void addBusEdge(int a, int b, double distKm, int busId) {
        graph.putIfAbsent(a, new ArrayList<>());
        graph.putIfAbsent(b, new ArrayList<>());
        graph.get(a).add(new Edge(b, distKm, busId));
        graph.get(b).add(new Edge(a, distKm, busId));
    }

    static void addBusRoute(int busId, int[] stops, Map<String, Double> knownDistances, double defaultDistKm) {
        for (int i = 0; i < stops.length - 1; i++) {
            int a = stops[i], b = stops[i + 1];
            String key1 = a + "-" + b, key2 = b + "-" + a;
            double d = knownDistances.getOrDefault(key1, knownDistances.getOrDefault(key2, defaultDistKm));
            addBusEdge(a, b, d, busId);
        }
    }

    static double calculateFare(double distanceKm) {
        if (distanceKm <= 5) return 15;
        double extraBlocks = Math.ceil((distanceKm - 5) / 5.0);
        return 15 + extraBlocks * 5;
    }

    static void dfsAllRoutes(int current, int target,
                             Set<Integer> visited,
                             List<Integer> path,
                             int prevBusId,
                             double distSoFar,
                             int transfersSoFar,
                             List<RouteInfo> result,
                             double kmToMinFactor,
                             int transferPenaltyMin) {

        visited.add(current);
        path.add(current);

        if (current == target) {
            double totalTime = distSoFar * kmToMinFactor + transfersSoFar * transferPenaltyMin;
            double fare = calculateFare(distSoFar);
            result.add(new RouteInfo(new ArrayList<>(path), distSoFar, transfersSoFar, totalTime, fare));
        } else {
            for (Edge e : graph.getOrDefault(current, Collections.emptyList())) {
                if (!visited.contains(e.to)) {
                    int nextTransfers = transfersSoFar;
                    if (prevBusId != -1 && e.busId != prevBusId)
                        nextTransfers++; 
                    dfsAllRoutes(e.to, target, visited, path, e.busId,
                            distSoFar + e.distKm, nextTransfers, result,
                            kmToMinFactor, transferPenaltyMin);
                }
            }
        }

        path.remove(path.size() - 1);
        visited.remove(current);
    }

    static void printRoute(RouteInfo r) {
        for (int j = 0; j < r.stations.size(); j++) {
            System.out.print(r.stations.get(j));
            if (j != r.stations.size() - 1) System.out.print(" -> ");
        }
        System.out.printf("\nDistance: %.2f km | Transfers: %d | Time: %.2f min | Fare: ₹%.2f\n",
                r.distanceKm, r.transfers, r.timeMin, r.fare);
    }

    // ---------- MAIN ----------
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        try {
            System.out.println("===============================================");
            System.out.println("FINAL ROUTE PROJECT - PUBLIC TRANSPORT SYSTEM");
            System.out.println("===============================================");
            System.out.println("1. Bus Network");
            System.out.println("2. Train Network");
            System.out.print("Select mode of transport (1 or 2): ");

            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number (1 or 2).");
                return;
            }
            int mode = sc.nextInt();

            if (mode == 2) {

                DNode head = buildTrainLine();
                DNode tail = head;
                while (tail.next != null) tail = tail.next;

                printTrainForward(head);
                printTrainBackward(tail);

                Scanner sc2 = new Scanner(System.in);

                System.out.print("\nEnter starting station (1-8): ");
                int start = sc2.nextInt();

                System.out.print("Enter destination station (18): ");
                int end = sc2.nextInt();

                if (start < 1 || start > 8 || end < 1 || end > 8) {
                    System.out.println("Invalid stations. Must be between 1-8.");
                    return;
                }

                int stops = Math.abs(end - start);
                int travelTime = stops * 4;
                int fare = calculateTrainFare(stops);

                int eta = 9;

                System.out.println("\n===================================");
                System.out.println("TRAIN ROUTE SUMMARY");
                System.out.println("===================================");
                System.out.println("Start Station: " + start);
                System.out.println("End Station: " + end);
                System.out.println("Stops Travelled: " + stops);
                System.out.println("Travel Time: " + travelTime + " minutes");
                System.out.println("Fare: ₹" + fare);
                System.out.println("Next Train Arrives In: " + eta + " minutes");
                return;
            }

            else if (mode != 1) {
                System.out.println("Invalid choice. Please restart and select 1 or 2.");
                return;
            }

            // ------------------- BUS SYSTEM (UNCHANGED) -------------------
            final double DEFAULT_DIST_KM = 1.0;
            final double KM_TO_MIN = 2.0;
            final int TRANSFER_PENALTY_MIN = 7;

            Map<String, Double> known = new HashMap<>();
            known.put("1-2", 2.0); known.put("1-3", 1.5); known.put("1-4", 2.2);
            known.put("2-3", 0.5); known.put("2-5", 1.7); known.put("2-6", 1.3);
            known.put("3-7", 2.0); known.put("3-8", 1.1); known.put("6-7", 0.4);
            known.put("7-8", 1.0); known.put("4-9", 2.4); known.put("4-10", 1.8);
            known.put("8-9", 0.6); known.put("9-10", 0.7); known.put("5-15", 2.1);
            known.put("5-11", 1.2); known.put("15-11", 0.8); known.put("6-11", 0.2);
            known.put("7-12", 1.5); known.put("8-13", 2.9); known.put("11-12", 1.7);
            known.put("12-13", 0.7); known.put("10-13", 2.3); known.put("10-14", 2.0);
            known.put("13-14", 1.0);

            int[] bus1 = {1, 2, 3, 4, 5};
            int[] bus2 = {3, 6, 7, 8, 9};
            int[] bus3 = {5, 9, 10, 11, 12};
            int[] bus4 = {2, 6, 10, 13, 14};
            int[] bus5 = {4, 8, 12, 13, 15};

            addBusRoute(1, bus1, known, DEFAULT_DIST_KM);
            addBusRoute(2, bus2, known, DEFAULT_DIST_KM);
            addBusRoute(3, bus3, known, DEFAULT_DIST_KM);
            addBusRoute(4, bus4, known, DEFAULT_DIST_KM);
            addBusRoute(5, bus5, known, DEFAULT_DIST_KM);

            System.out.println("\n===============================================");
            System.out.println("BUS NETWORK SIMULATOR");
            System.out.println("===============================================");
            System.out.println("Fare rule: ₹15 for first 5 km, then +₹5 per 5 km extra.");
            System.out.println("Transfer penalty: 7 minutes per bus change.\n");

            System.out.print("Enter starting stop (1–15): ");
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number between 1 and 15.");
                return;
            }
            int start = sc.nextInt();

            System.out.print("Enter destination stop (1–15): ");
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number between 1 and 15.");
                return;
            }
            int end = sc.nextInt();

            if (start < 1 || start > 15 || end < 1 || end > 15) {
                System.out.println("Stops must be between 1 and 15. Please restart.");
                return;
            }

            if (!graph.containsKey(start) || !graph.containsKey(end)) {
                System.out.println("Invalid stop entered. Please check available stops.");
                return;
            }

            List<RouteInfo> allRoutes = new ArrayList<>();
            dfsAllRoutes(start, end, new HashSet<>(), new ArrayList<>(),
                    -1, 0.0, 0, allRoutes, KM_TO_MIN, TRANSFER_PENALTY_MIN);

            if (allRoutes.isEmpty()) {
                System.out.println("No route found between these stops.");
                return;
            }

            System.out.println("\nALL POSSIBLE ROUTES:");
            int routeNum = 1;
            for (RouteInfo r : allRoutes) {
                System.out.print("Route " + routeNum++ + ": ");
                for (int j = 0; j < r.stations.size(); j++) {
                    System.out.print(r.stations.get(j));
                    if (j != r.stations.size() - 1) System.out.print(" -> ");
                }
                System.out.printf("\nDistance: %.2f km | Transfers: %d | Time: %.2f min | Fare: ₹%.2f\n\n",
                        r.distanceKm, r.transfers, r.timeMin, r.fare);
            }

            RouteInfo bestDist = allRoutes.get(0);
            RouteInfo bestTime = allRoutes.get(0);
            for (RouteInfo r : allRoutes) {
                if (r.distanceKm < bestDist.distanceKm) bestDist = r;
                if (r.timeMin < bestTime.timeMin) bestTime = r;
            }

            System.out.println("===============================================");
            System.out.println("BEST ROUTES SUMMARY");
            System.out.println("===============================================");
            System.out.println("Shortest Route:");
            printRoute(bestDist);
            System.out.println("\nFastest Route:");
            printRoute(bestTime);

            System.out.println("\nProgram completed successfully.");
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        } finally {
            sc.close();
        }
    }
}
                        
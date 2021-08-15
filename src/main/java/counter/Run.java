package counter;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class Run {
    private static final TreeMap<String, HashMap<String, Long>> urlCountMap = new TreeMap<>();

    public static void main(String[] args) {
        urlHitCount(args);
    }
    public static void urlHitCount(String[] args) {
        Collections.synchronizedNavigableMap(urlCountMap);
        String fileName;

        if (args != null && args.length != 0) {
            fileName = args[0];
            try {
                extractInfoFromFile(fileName);
            } catch (Exception e) {
                System.out.println("Exception occured while reading file : " + e.getClass().getName());
            }
        }
    }
    private static boolean extractInfoFromFile(String fileName){

        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(str-> urlHitCounter(str));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        // System.out.println(urlCountMap);
        for (Map.Entry<String, HashMap<String, Long>> entry : urlCountMap.entrySet()) {
            System.out.println(entry.getKey());
            HashMap sortedMap = sortByValues(entry.getValue());
            Set sortedKeys = sortedMap.entrySet();
            Iterator iterator = sortedKeys.iterator();
            while(iterator.hasNext()) {
                Map.Entry sortedEntry = (Map.Entry)iterator.next();
                System.out.println(sortedEntry.getKey()+" "+sortedEntry.getValue());
            }

        }
        return true;
    }

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        Comparator<Map.Entry<String, Long>> valueComparator = (e1, e2) -> {
            Long v1 = e1.getValue();
            Long v2 = e2.getValue();
            if (v1 == v2) {
                // if hit count is same, sort alphabetically.
                String k1 = e1.getKey();
                String k2 = e2.getKey();
                return k1.compareTo(k2);
            }
            return v2.compareTo(v1);
        };

        List<Map.Entry<String, Long>> listOfEntries = new ArrayList<Map.Entry<String, Long>>(map.entrySet());

        Collections.sort(listOfEntries, valueComparator);

        LinkedHashMap<String, Long> sortedHashMap = new LinkedHashMap<>(listOfEntries.size());
        for(Map.Entry<String, Long> entry : listOfEntries){
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }

        return sortedHashMap;
    }
    private static void urlHitCounter(String urlData) {

        String dateInEpoch = urlData.substring(0, urlData.indexOf("|"));
        String url = urlData.substring(urlData.indexOf("|")+1);

        //convert epoch to date in GMT
        ZoneId zoneId = ZoneId.of("GMT");
        Instant instant = Instant.ofEpochMilli(Long.parseLong(dateInEpoch)*1000);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy zzz");
        String date = dateTimeFormatter.format(zonedDateTime);

        HashMap<String, Long>tmap = urlCountMap.getOrDefault(date, new HashMap<>());
        Long hitCount = tmap.getOrDefault(url, (long) 0)+1;
        tmap.put(url, hitCount);

        urlCountMap.put(date, tmap);
    }
}

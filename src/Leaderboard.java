import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * @author Keinan Gilad
 *
 */
public class Leaderboard extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private List<UserData> list = new ArrayList<UserData>();
    private BufferedReader br = null;
    private ValueComparator comparator = new ValueComparator();
    private String filename = "leaderboard.txt";

    public Leaderboard() {
        super();

        loadFromFile();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.getWriter().println(convertMapToJson());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap != null) {
            String name = null;
            if (parameterMap.containsKey("name")) {
                name = parameterMap.get("name")[0];
            }
            String value = null;
            if (parameterMap.containsKey("value")) {
                value = parameterMap.get("value")[0];
            }

            String color = null;
            if (parameterMap.containsKey("color")) {
                color = parameterMap.get("color")[0];
            }

            updateMap(name, value, color, true);
        }

        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap != null) {
            String name = null;
            if (parameterMap.containsKey("name")) {
                name = parameterMap.get("name")[0];
            }

            updateMap(name, null, null, false);
        }

        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        loadFromFile();

        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    private void updateMap(String name, String value, String color, boolean toAdd) {
        if (name != null && !name.equals("null")) {
            name = name.trim();

            Integer valParsed = 0;
            if (value != null) {
                value = value.trim();
                try {
                    valParsed = Integer.valueOf(value);
                } catch (Exception e) {
                    // Nothing to do.
                }
            }

            int match = -1;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getName().equals(name)) {
                    match = i;
                    break;
                }
            }
            if (match != -1 && !toAdd) {
                list.remove(match);
            }

            if (toAdd) {
                UserData data = new UserData(name, valParsed, color);
                list.add(data);
            }

            writeToFile();
        }
    }

    private void writeToFile() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            String mapJson = convertMapToJson();
            bw.write(mapJson);
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        } catch (FileNotFoundException e) {
            // If not found, we will create it first.
            writeToFile();
        }

        if (br == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            String mapJson = sb.toString().trim();
            convertJsonToMap(mapJson);
        } catch (Exception e) {

        }
    }

    private String convertMapToJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        if (!list.isEmpty()) {
            Collections.sort(list, comparator);
            for (UserData data : list) {
                sb.append("{");
                sb.append(String.format("\"name\":\"%s\"", data.getName()));
                sb.append(",");
                sb.append(String.format("\"value\":\"%s\"", data.getValue()));
                sb.append(",");
                sb.append(String.format("\"color\":\"%s\"", data.getColor()));
                sb.append("}");
                sb.append(",");
            }

            sb.deleteCharAt(sb.lastIndexOf(","));// Removing the last index of ","
        }

        sb.append("]");
        return sb.toString();
    }

    private void convertJsonToMap(String mapJson) {
        list.clear();

        mapJson = mapJson.trim();
        JsonArray array = new JsonParser().parse(mapJson).getAsJsonArray();
        if (array.size() < 1) {
            return; // no input
        }

        for (int i = 0; i < array.size(); i++) {
            JsonObject jsonElement = array.get(i).getAsJsonObject();
            String name = jsonElement.get("name").getAsString();
            Integer value = jsonElement.get("value").getAsInt();
            String color = jsonElement.get("color").getAsString();
            UserData data = new UserData(name, value, color);
            list.add(data);
        }
    }

    public class ValueComparator implements Comparator<UserData> {
        @Override
        public int compare(UserData x, UserData y) {
            return y.getValue().compareTo(x.getValue());
        }
    }

    public class UserData {
        private String name;
        private String color;
        private Integer value;

        public UserData(String name, Integer value, String color) {
            this.name = name;
            this.color = color;
            this.value = value;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}

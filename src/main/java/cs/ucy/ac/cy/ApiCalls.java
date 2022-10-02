package cs.ucy.ac.cy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ApiCalls {

    public static void sendToAPI(String function, float exportedEnergy, float selfConsumptionEnergy, float importedEnergy, float production, float consumption, float co2emissions, float comfortCost, float comfortPercentage, float execTime) throws IOException, InterruptedException {
        JSONObject jsonObject = new JSONObject()
                .put("function_name", String.valueOf(function))
                .put("exported", String.valueOf(exportedEnergy))
                .put("selfconsumption", String.valueOf(selfConsumptionEnergy))
                .put("imported", String.valueOf(importedEnergy))
                .put("production", String.valueOf(production))
                .put("consumption", String.valueOf(consumption))
                .put("co2_emissions", String.valueOf(co2emissions))
                .put("comfort_error", String.valueOf(comfortCost))
                .put("comfort", String.valueOf(comfortPercentage))
                .put("execution_time", String.valueOf(execTime));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://10.16.30.215/api/update-results"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode() + " - Results send to server");
    }

    public static List<String[]> getRulesFromAPI() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://10.16.30.215/api/get-metarules"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.statusCode() + " - Rules received from server");

        List<String[]> rules = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(response.body());

        for (int i = 0; i < jsonArray.length(); i++) {
            String[] rule = new String[2];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime dateTime = LocalDateTime.parse(jsonArray.getJSONObject(i).getString("time"), formatter);
            rule[0] = dateTime.format(formatter2);
            rule[1] = jsonArray.getJSONObject(i).getString("action");

            rules.add(rule);
        }

        return rules;
    }
}

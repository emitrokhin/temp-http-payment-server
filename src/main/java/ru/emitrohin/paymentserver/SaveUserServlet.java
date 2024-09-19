package ru.emitrohin.paymentserver;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/save-user-data")
public class SaveUserServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:6543/postgres";
    private static final String USER = "postgres.bejpczdkansztszqdbvd";
    private static final String PASS = "Wecmax-0wenma-vomsej";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws  IOException {
        // Чтение данных из запроса
        var reader = req.getReader();
        var objectMapper = new ObjectMapper();
        var userData = objectMapper.readValue(req.getReader(), Map.class);

        // Извлечение данных
        long id = Long.parseLong(userData.get("id").toString());
        String firstName = (String) userData.get("first_name");
        String lastName = (String) userData.getOrDefault("last_name", null);
        String username = (String) userData.getOrDefault("username", null);
        String languageCode = (String) userData.getOrDefault("language_code", null);
        boolean allowsWriteToPm = (boolean) userData.getOrDefault("allows_write_to_pm", false);
        String photoUrl = (String) userData.getOrDefault("photo_url", null);

        String sql = """
            INSERT INTO all_incoming_users (id, first_name, last_name, username, language_code, allows_write_to_pm, photo_url)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                first_name = EXCLUDED.first_name,
                last_name = EXCLUDED.last_name,
                username = EXCLUDED.username,
                language_code = EXCLUDED.language_code,
                allows_write_to_pm = EXCLUDED.allows_write_to_pm,
                photo_url = EXCLUDED.photo_url;
            """;

        // Сохранение в базу данных
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setLong(1, id);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, username);
            statement.setString(5, languageCode);
            statement.setBoolean(6, allowsWriteToPm);
            statement.setString(7, photoUrl);

            statement.executeUpdate();

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("User data saved successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
package ru.emitrohin.paymentserver;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            path = "/index.html";
        } else if (path.equals("/success")) {
            path = "/success.html";
        } else if (path.equals("/fail")) {
            path = "/fail.html";
        }

        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get("src/main/resources" + path)));
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            content = "<h1>404 - Not Found</h1>";
        }

        resp.getWriter().println(content);
    }
}

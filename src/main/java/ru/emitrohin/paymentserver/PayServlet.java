package ru.emitrohin.paymentserver;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;

@WebServlet("/pay")
public class PayServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var firstName = req.getParameter("firstName");
        var lastName = req.getParameter("lastName");
        var phone = req.getParameter("phone");
        var email = req.getParameter("email");

        // Проверка параметров
        if (isNullOrEmpty(firstName) || isNullOrEmpty(lastName) || isNullOrEmpty(phone) || isNullOrEmpty(email)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        req.setAttribute("email", email);

        try {
            req.getRequestDispatcher("/jsp/pay.jsp").forward(req, resp);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
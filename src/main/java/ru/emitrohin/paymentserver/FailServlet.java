package ru.emitrohin.paymentserver;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/fail")
public class FailServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(FailServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            req.getRequestDispatcher("/jsp/fail.jsp").forward(req, resp);
        } catch (Exception e) {
            logger.error("Ошибка при переходе на страницу fail.jsp: ", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DeserializationServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            byte[] serializedData = request.getInputStream().readAllBytes();  // User-provided input

            // Security fix (analysis):
            // Do not perform Java native deserialization on untrusted network input (prevents CWE-502 RCE)
            // and avoid reflecting deserialized content back to the client (prevents CWE-79 XSS).
            // Instead, reject arbitrary serialized payloads and return a safe, plain-text message.
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().println("Deserialization of arbitrary Java objects from untrusted input is not supported.");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error: " + e.getMessage());

    }
}


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.InvalidClassException;
import java.util.Set;
import java.util.HashSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DeserializationServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            byte[] serializedData = request.getInputStream().readAllBytes();  // User-provided input

            // Enforce a reasonable maximum payload size (1 MiB) to limit attack surface
            if (serializedData.length > 1 * 1024 * 1024) {
                response.getWriter().println("Error: payload too large");
                return;
            }

            // Use a safe ObjectInputStream that enforces an explicit allow-list of classes.
            SafeObjectInputStream ois = new SafeObjectInputStream(new ByteArrayInputStream(serializedData));
            Object obj = ois.readObject();
            ois.close();

            response.getWriter().println("Deserialized object: " + obj.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error: " + e.getMessage());
        }
    }

    // Safe ObjectInputStream that enforces an explicit allow-list of classes.
    // This prevents arbitrary class resolution during deserialization.
    private static class SafeObjectInputStream extends ObjectInputStream {
        private static final Set<String> ALLOWED_CLASSES = new HashSet<String>() {{
            add("java.util.HashMap");
            add("java.util.LinkedHashMap");
            add("java.util.ArrayList");
            add("java.lang.String");
            add("java.lang.Integer");
            add("java.lang.Long");
            add("java.lang.Boolean");
        }};

        public SafeObjectInputStream(java.io.InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(java.io.ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String className = desc.getName();
            if (ALLOWED_CLASSES.contains(className)) {
                return super.resolveClass(desc);
            } else {
                throw new InvalidClassException("Unauthorized deserialization attempt", className);
            }
        }
    }
}



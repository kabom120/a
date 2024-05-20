package Controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import model.ProductModel;
import model.game;

@WebServlet("/AddGame")
@MultipartConfig()
public class UploadGame extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static String SAVE_DIR = "img";
    static ProductModel GameModels = new ProductModelDM();
    
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDateTime now = LocalDateTime.now();
    
    public UploadGame() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/plain");
        out.write("Error: GET method is used but POST method is required");
        out.close();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Assicurarsi che la directory di salvataggio esista
        String savePath = request.getServletContext().getRealPath("") + File.separator + SAVE_DIR;
        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }

        game g1 = new game();
        String message = "upload =\n";

        if (request.getParts() != null && request.getParts().size() > 0) {
            for (Part part : request.getParts()) {
                String fileName = extractFileName(part);
                if (fileName != null && !fileName.isEmpty()) {
                    // Validare il tipo di file e l'estensione
                    if (isValidFileType(part)) {
                        // Generare un nome univoco per il file
                        String newFileName = UUID.randomUUID().toString() + "_" + sanitizeFileName(fileName);
                        part.write(savePath + File.separator + newFileName);
                        g1.setImg(newFileName);
                        message = message + newFileName + "\n";
                    } else {
                        request.setAttribute("error", "Tipo di file non valido. Si prega di caricare un file valido.");
                    }
                } else {
                    request.setAttribute("error", "Errore: Bisogna selezionare almeno un file");
                }
            }
        }

        g1.setName(request.getParameter("nomeGame"));
        g1.setYears(request.getParameter("years"));
        g1.setAdded(dtf.format(now));
        g1.setQuantity(Integer.valueOf(request.getParameter("quantita")));
        g1.setPEG(Integer.valueOf(request.getParameter("PEG")));
        g1.setIva(Integer.valueOf(request.getParameter("iva")));
        g1.setGenere(request.getParameter("genere"));
        g1.setDesc(request.getParameter("desc"));
        g1.setPrice(Float.valueOf(request.getParameter("price")));

        try {
            GameModels.doSave(g1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("stato", "success!");
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/gameList?page=admin&sort=added DESC");
        dispatcher.forward(request, response);
    }

    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length() - 1);
            }
        }
        return null;
    }

    private boolean isValidFileType(Part part) {
        String mimeType = part.getContentType();
        // Definire una lista di tipi MIME consentiti
        List<String> allowedMimeTypes = Arrays.asList("image/png", "image/jpeg", "image/gif");
        return allowedMimeTypes.contains(mimeType);
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
    }
}

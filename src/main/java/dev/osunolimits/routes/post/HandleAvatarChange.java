package dev.osunolimits.routes.post;

import dev.osunolimits.main.App;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import spark.Request;
import spark.Response;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import javax.servlet.MultipartConfigElement;

import net.coobird.thumbnailator.Thumbnails;

public class HandleAvatarChange extends Shiina {

    private MultipartConfigElement multipartConfig;
    private static final String AVATAR_DIR = App.env.get("AVATARFOLDER");
    private static final int MAX_FILE_SIZE = (Integer.parseInt(App.env.get("MAXFILESIZE"))) * 1024 * 1024;
    private static final int MAX_REQUEST_SIZE = (Integer.parseInt(App.env.get("MAXREQUESTSIZE"))) * 1024 * 1024;

    public HandleAvatarChange() {
        multipartConfig = new MultipartConfigElement(".temp/", MAX_REQUEST_SIZE, MAX_REQUEST_SIZE, 1);
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 0);

        if (!shiina.loggedIn) {
            res.redirect("/login");
            return notFound(res, shiina);
        }

        int userId = shiina.user.id;
        req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfig);

        try {
            if (req.raw().getParts().size() > 0) {
                var part = req.raw().getPart("avatar");
                if (part != null) {
                    String fileName = part.getSubmittedFileName();

                    if (fileName != null && fileName.toLowerCase().endsWith(".png") && part.getSize() <= MAX_FILE_SIZE) {
                        File avatarDir = new File(AVATAR_DIR);
                        if (!avatarDir.exists()) {
                            avatarDir.mkdirs();
                        }

                        Path finalAvatarPath = Path.of(AVATAR_DIR, userId + ".png");

                        for (File file : avatarDir.listFiles()) {
                            if (file.getName().equals(userId + ".png") || file.getName().matches(userId + "\\.(jpg|gif)")) {
                                file.delete();
                            }
                        }

                        // Resize directly from the InputStream and write to the final path
                        try (InputStream input = part.getInputStream()) {
                            Thumbnails.of(input)
                                    .size(500, 500)
                                    .outputFormat("png")
                                    .toFile(finalAvatarPath.toFile());
                        }

                        res.header("Cache-Control", "no-cache, no-store, must-revalidate");
                        res.header("Pragma", "no-cache");
                        res.header("Expires", "0");
                        res.redirect("/settings?info=Avatar uploaded successfully! If it didn't update, hit CTRL+F5");
                    } else {
                        res.redirect("/settings?error=Invalid file type or size exceeds limit.");
                    }
                } else {
                    res.redirect("/settings?error=No file uploaded.");
                }
            } else {
                res.redirect("/settings?error=No parts found in the request.");
            }
        } catch (Exception e) {
            res.redirect("/settings?error=Error processing the upload: " + e.getMessage());
        }

        return notFound(res, shiina);
    }
}

package edu.univ.eventos;

import edu.univ.eventos.config.JpaConfig;
import edu.univ.eventos.controller.ApiController;
import edu.univ.eventos.service.AppException;
import io.javalin.Javalin;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        JpaConfig.em().close(); // trigger init and seed
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.showJavalinBanner = false;
        });

        app.exception(AppException.class, (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
        app.exception(RuntimeException.class, (e, ctx) -> {
            if (e.getMessage() != null && e.getMessage().contains("autoriz")) ctx.status(403); else ctx.status(401);
            ctx.json(Map.of("error", e.getMessage()));
        });

        new ApiController().register(app);

        app.get("/", ctx -> ctx.redirect("/index.html"));
        Runtime.getRuntime().addShutdownHook(new Thread(JpaConfig::shutdown));
        app.start(7000);
    }
}

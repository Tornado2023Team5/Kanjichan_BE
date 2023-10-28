package com.github.tornado2023team5.kanjichan.service.mention;

import com.github.tornado2023team5.kanjichan.model.function.ShopCategory;
import com.github.tornado2023team5.kanjichan.service.FunctionCallService;
import com.github.tornado2023team5.kanjichan.service.GoogleMapsService;
import com.github.tornado2023team5.kanjichan.service.SetupScheduleService;
import com.google.maps.errors.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommandSearchSpotsService {
    public final SetupScheduleService setupScheduleService;
    public final GoogleMapsService googleMapsService;
    public final FunctionCallService functionCallService;
    public final CommandSetDestinationService commandSetDestinationService;

    public void searchSpots(String id, StringBuilder reply, String text, String location) throws IOException, InterruptedException, ApiException {
        var session = setupScheduleService.getSession(id);
        if (session == null) {
            reply.append("まずは予定を立てるウサ！🥕\n");
            return;
        }
        if (session.getLocation() == null && location == null) {
            reply.append("活動場所を設定してください。\n 例: \n @bot \n 渋谷で遊びたい！");
            return;
        }
        if (text == null) {
            reply.append("何をしたいか教えてください。\n 例: \n @bot \n 焼肉食べたい！");
            return;
        }
        if (location != null) commandSetDestinationService.setDestination(id, reply, location, true);
        ShopCategory category = functionCallService.pickup(text);

        var results = googleMapsService.getShopInfo(session.getLocation(), category);
        session.getResultsList().add(results);

        reply.append("「").append(session.getLocation()).append("」周辺の").append("「").append(category.getValue()).append("」").append("はこんな所があるウサ！！\uD83D\uDC30\n\n");
        reply.append(results.stream().map(place -> "◦ " + place.name + "\n" +
                "レビュー: " + GoogleMapsService.getRatingStars(place.rating) + " " + place.rating + "\n" +
                place.url).collect(Collectors.joining("\n\n")));
        reply.append("\n\n").append("他にも遊び場所を追加するウサ？🌕\uD83D\uDC30✨\n").append("予定を確定するなら確定！って言ってほしいウサ！！\uD83E\uDD55\n");
    }
}

package com.laioffer.twitch.recommendation;


import com.laioffer.twitch.db.entity.ItemEntity;
import com.laioffer.twitch.db.entity.UserEntity;
import com.laioffer.twitch.external.TwitchService;
import com.laioffer.twitch.external.model.Video;
import com.laioffer.twitch.favorite.FavoriteService;
import com.laioffer.twitch.model.TypeGroupedItemList;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.laioffer.twitch.external.model.Clip;
import com.laioffer.twitch.external.model.Stream;


@Service
public class RecommendationService {


    private static final int MAX_GAME_SEED = 3;
    private static final int PER_PAGE_ITEM_SIZE = 20;


    private final TwitchService twitchService;
    private final FavoriteService favoriteService;


    public RecommendationService(TwitchService twitchService, FavoriteService favoriteService) {
        this.twitchService = twitchService;
        this.favoriteService = favoriteService;
    }

//@Cacheable("recommend_items")表示 recommendItems 方法的结果将被缓存。
// 缓存的名称为 "recommend_items"。当再次调用该方法时，如果有对应的缓存项，将直接使用缓存的数据，而不是重新执行方法。
    @Cacheable("recommend_items")
    public TypeGroupedItemList recommendItems(UserEntity userEntity) {
        List<String> gameIds;
        Set<String> exclusions = new HashSet<>();
        if (userEntity == null) {
            gameIds  = twitchService.getTopGameIds();
        } else {
            List<ItemEntity> items = favoriteService.getFavoriteItems(userEntity);
            if (items.isEmpty()) {
                gameIds = twitchService.getTopGameIds();
            } else {
                Set<String> uniqueGameIds = new HashSet<>();
                for (ItemEntity item : items) {
                    uniqueGameIds.add(item.gameId());
                    exclusions.add(item.twitchId());
                }
                gameIds = new ArrayList<>(uniqueGameIds);
            }
        }


        int gameSize = Math.min(gameIds.size(), MAX_GAME_SEED);
        int perGameListSize = PER_PAGE_ITEM_SIZE / gameSize;


        List<ItemEntity> streams = recommendStreams(gameIds, exclusions);
        List<ItemEntity> clips = recommendClips(gameIds.subList(0, gameSize), perGameListSize, exclusions);
        List<ItemEntity> videos = recommendVideos(gameIds.subList(0, gameSize), perGameListSize, exclusions);


        return new TypeGroupedItemList(streams, videos, clips);
    }


    private List<ItemEntity> recommendStreams(List<String> gameIds, Set<String> exclusions) {
        List<Stream> streams = twitchService.getStreams(gameIds, PER_PAGE_ITEM_SIZE);
        List<ItemEntity> resultItems = new ArrayList<>();
        for (Stream stream: streams) {
            if (!exclusions.contains(stream.id())) {
                resultItems.add(new ItemEntity(stream));
            }
        }
        return resultItems;
    }


    private List<ItemEntity> recommendVideos(List<String> gameIds, int perGameListSize, Set<String> exclusions) {
        List<ItemEntity> resultItems = new ArrayList<>();
        for (String gameId : gameIds) {
            List<Video> listPerGame = twitchService.getVideos(gameId, perGameListSize);
            for (Video video : listPerGame) {
                if (!exclusions.contains(video.id())) {
                    resultItems.add(new ItemEntity(gameId, video));
                }
            }
        }
        return resultItems;
    }


    private List<ItemEntity> recommendClips(List<String> gameIds, int perGameListSize, Set<String> exclusions) {
        List<ItemEntity> resultItem = new ArrayList<>();
        for (String gameId : gameIds) {
            List<Clip> listPerGame = twitchService.getClips(gameId, perGameListSize);
            for (Clip clip : listPerGame) {
                if (!exclusions.contains(clip.id())) {
                    resultItem.add(new ItemEntity(clip));
                }
            }
        }
        return resultItem;
    }
}


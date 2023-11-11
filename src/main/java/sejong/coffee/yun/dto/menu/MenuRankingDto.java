package sejong.coffee.yun.dto.menu;

import lombok.Getter;
import org.springframework.data.redis.core.ZSetOperations;
import sejong.coffee.yun.domain.order.menu.Menu;

@Getter
public class MenuRankingDto {

    /**
     * public record Response(String menuTitle, Double score) {
     *
     *         public static Response convertToResponseRankingDto(ZSetOperations.TypedTuple<String> typedTuple) {
     *             return new Response(typedTuple.getValue(), typedTuple.getScore());
     *         }
     *     }
     */
    public record Response(String menuTitle, Double score, int viewCount, int orderCount) {
        public static Response convertToResponseRankingDto(ZSetOperations.TypedTuple<String> typedTuple, Menu menu) {
            return new Response(typedTuple.getValue(), typedTuple.getScore(), menu.getViewCount(), menu.getOrderCount());
        }

        public static Response from(String menuTitle, Double score, int viewCount, int orderCount) {
            return new Response(menuTitle, score, viewCount, orderCount);
        }
    }
}

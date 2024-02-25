package sejong.coffee.yun.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.dto.menu.MenuDto;
import sejong.coffee.yun.dto.menu.MenuPageDto;
import sejong.coffee.yun.dto.menu.MenuRankingDto;
import sejong.coffee.yun.repository.menu.MenuRepository;
import sejong.coffee.yun.service.MenuService;

import javax.validation.Valid;
import java.util.*;

import static sejong.coffee.yun.dto.menu.MenuDto.Request;
import static sejong.coffee.yun.dto.menu.MenuDto.Response;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
@Validated
@Slf4j
public class MenuController {

    private final MenuService menuService;
    private final MenuRepository menuRepository;
    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Object> objectRedisTemplate;

    @PostMapping("")
    ResponseEntity<Response> createMenu(@RequestBody @Valid Request request) {

        Menu menu = menuService.create(request);
        Response response = new Response(menu);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/page/{pageNum}")
    ResponseEntity<MenuPageDto.Response> findAll(@PathVariable int pageNum) {
        PageRequest pageRequest = PageRequest.of(pageNum, 5);

        Page<Response> allMenu = menuService.findAll(pageRequest);
        List<Response> responses = allMenu.stream().toList();

        MenuPageDto.Response response = new MenuPageDto.Response(pageNum, responses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/caching/page/{pageNum}")
    ResponseEntity<MenuPageDto.Response> findAllByCaching(@PathVariable int pageNum) {
        PageRequest pageRequest = PageRequest.of(pageNum, 5, Sort.by("id"));
        Page<Response> allMenu = menuService.findAllByCaching(pageRequest);
        List<Response> responses = allMenu.stream().toList();


        MenuPageDto.Response response = new MenuPageDto.Response(pageNum, responses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/menu")
    ResponseEntity<MenuDto.Response> searchMenu(@RequestParam("title") String menuTitle) {

        Response response = menuService.menuSearch(menuTitle);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/ranking-list")
    ResponseEntity<List<MenuRankingDto.Response>> searchRankingList() {

        List<MenuRankingDto.Response> responses = menuService.searchRankList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/redis")
    public Object findAll() {
        List<Map<String, String>> result = new ArrayList<>();

        for (String cacheName : cacheManager.getCacheNames()) {
            Set<String> keys = redisTemplate.keys(cacheName + "*");
            for (String key : keys) {
                Object value = redisTemplate.opsForValue().get(key);
                Map<String, String> entry = new HashMap<>();
                entry.put("Key", key);
                entry.put("Value", value != null ? value.toString() : null);
                result.add(entry);
            }
        }

        return result;
    }

    @GetMapping("/async-redis")
    public void asyncWriteBack() {
        menuService.refreshPopularMenusInRedis();
    }

    @GetMapping("/sync-redis")
    public void syncWriteBack() {
        Set<String> keys = Optional.ofNullable(redisTemplate.keys("menu::*")).orElse(Collections.emptySet());

        for(String key : keys){
            try {
                Menu popularMenu = (Menu) objectRedisTemplate.opsForValue().get(key);
                if (popularMenu != null) {
                    Menu findMenu = menuRepository.findByTitle(popularMenu.getTitle());
                    if (findMenu != null) {
                        Double score = redisTemplate.opsForZSet().score("ranking", popularMenu.getTitle());
                        if (score != null) {
                            menuService.updateMenu(findMenu, popularMenu.getOrderCount(), popularMenu.getViewCount(), score);
                        }
                    }
                }
            } catch(Exception ex) {
                log.error("Failed to process key: {}", key, ex);
            }
        }

        // 모든 동기 작업이 완료되면 redisTemplate.delete("menu::*") 작업을 실행
        redisTemplate.delete("menu::*");

    }

}

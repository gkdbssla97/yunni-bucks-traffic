package sejong.coffee.yun.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sejong.coffee.yun.domain.order.menu.Menu;
import sejong.coffee.yun.dto.menu.MenuDto;
import sejong.coffee.yun.dto.menu.MenuPageDto;
import sejong.coffee.yun.dto.menu.MenuRankingDto;
import sejong.coffee.yun.mapper.CustomMapper;
import sejong.coffee.yun.service.MenuService;

import javax.validation.Valid;
import java.util.List;

import static sejong.coffee.yun.dto.menu.MenuDto.Request;
import static sejong.coffee.yun.dto.menu.MenuDto.Response;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
@Validated
@Slf4j
public class MenuController {

    private final MenuService menuService;
    private final CustomMapper customMapper;
    private final ObjectMapper objectMapper;

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
}

package com.game.controller;

import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.entity.Player;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/rest")
public class RestPlayerController {
    private PlayerService playerService;

    @Autowired
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping(value = "/players")
    public ResponseEntity<List<Player>> getAllPlayer(@RequestParam(value = "name", required = false) String name,
                                                     @RequestParam(value = "title", required = false) String title,
                                                     @RequestParam(value = "race", required = false) Race race,
                                                     @RequestParam(value = "profession", required = false) Profession profession,
                                                     @RequestParam(value = "after", required = false) Long after,
                                                     @RequestParam(value = "before", required = false) Long before,
                                                     @RequestParam(value = "banned", required = false) Boolean banned,
                                                     @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                                     @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                                     @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                                     @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                                     @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder order,
                                                     @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                                     @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
        Specification<Player> specification = Specification.where(playerService.selectByName(name)
                .and(playerService.selectByTitle(title))
                .and(playerService.selectByRace(race))
                .and(playerService.selectByProfession(profession))
                .and(playerService.selectByBirthday(after, before))
                .and(playerService.selectByBanned(banned))
                .and(playerService.selectByExperience(minExperience, maxExperience))
                .and(playerService.selectByLevel(minLevel, maxLevel)));

        return new ResponseEntity<>(playerService.getPlayersList(specification, pageable).getContent(), HttpStatus.OK);
    }

    @GetMapping(value = "/players/count")
    public ResponseEntity<Integer> getPlayersCount(@RequestParam(value = "name", required = false) String name,
                                                   @RequestParam(value = "title", required = false) String title,
                                                   @RequestParam(value = "race", required = false) Race race,
                                                   @RequestParam(value = "profession", required = false) Profession profession,
                                                   @RequestParam(value = "after", required = false) Long after,
                                                   @RequestParam(value = "before", required = false) Long before,
                                                   @RequestParam(value = "banned", required = false) Boolean banned,
                                                   @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                                   @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                                   @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                                   @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {
        Specification<Player> specification = Specification.where(playerService.selectByName(name)
                .and(playerService.selectByTitle(title))
                .and(playerService.selectByRace(race))
                .and(playerService.selectByProfession(profession))
                .and(playerService.selectByBirthday(after, before))
                .and(playerService.selectByBanned(banned))
                .and(playerService.selectByExperience(minExperience, maxExperience))
                .and(playerService.selectByLevel(minLevel, maxLevel)));

        return new ResponseEntity<>(playerService.getPlayersCount(specification), HttpStatus.OK);
    }

    @PostMapping(value = "/players")
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        Player responsePlayer = playerService.createPlayer(player);
        return new ResponseEntity<>(responsePlayer, HttpStatus.OK);
    }

    @GetMapping(value = "/players/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable String id) {
        Long parseId = playerService.checkId(id);
        Player responsePlayer = playerService.getPlayer(parseId);
        return new ResponseEntity<>(responsePlayer, HttpStatus.OK);
    }

    @PostMapping(value = "/players/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable String id,
                                               @RequestBody Player player) {
        Long parseId = playerService.checkId(id);
        Player responsePlayer = playerService.updatePlayer(parseId, player);
        return new ResponseEntity<>(responsePlayer, HttpStatus.OK);
    }

    @DeleteMapping(value = "/players/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable String id) {
        Long parseId = playerService.checkId(id);
        playerService.deletePlayer(parseId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

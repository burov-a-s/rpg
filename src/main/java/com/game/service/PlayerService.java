package com.game.service;

import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface PlayerService {
    Page<Player> getPlayersList(Specification<Player> playerSpecification, Pageable pageable);
    Integer getPlayersCount(Specification<Player> playerSpecification);
    Player createPlayer (Player player);
    Player getPlayer(Long id);
    Player updatePlayer(Long id, Player player);
    void deletePlayer(Long id);
    Long checkId(String id);

    Specification<Player> selectByName(String name);
    Specification<Player> selectByTitle(String title);
    Specification<Player> selectByRace(Race race);
    Specification<Player> selectByProfession (Profession profession);
    Specification<Player> selectByExperience(Integer minExperience, Integer maxExperience);
    Specification<Player> selectByLevel(Integer minLevel, Integer maxLevel);
    Specification<Player> selectByBirthday(Long after, Long before);
    Specification<Player> selectByBanned(Boolean banned);
}

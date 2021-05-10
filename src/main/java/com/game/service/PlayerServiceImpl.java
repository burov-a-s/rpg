package com.game.service;

import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exceptions.BadRequestException;
import com.game.exceptions.NotFoundException;
import com.game.entity.Player;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {

    private PlayerRepository playerRepository;

    @Autowired
    public void setPlayerRepository(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Page<Player> getPlayersList(Specification<Player> playerSpecification, Pageable pageable) {
        return playerRepository.findAll(playerSpecification, pageable);
    }

    @Override
    public Integer getPlayersCount(Specification<Player> playerSpecification) {
        return playerRepository.findAll(playerSpecification).size();
    }

    @Override
    public Player createPlayer(Player player) {
        if (player.getName() == null
                || player.getTitle() == null
                || player.getRace() == null
                || player.getProfession() == null
                || player.getBirthday() == null
                || player.getExperience() == null) {
            throw new BadRequestException();
        }

        checkPlayerName(player);
        checkPlayerTitle(player);
        checkPlayerExperience(player);
        checkPlayerBirthday(player);

        if (player.getBanned() == null) {
            player.setBanned(false);
        }

        player.setLevel(calculatePlayerLevel(player));
        player.setUntilNextLevel(calculatePlayerUntilNextLevel(player));

        return playerRepository.saveAndFlush(player);
    }

    @Override
    public Player getPlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new NotFoundException();
        }
        return playerRepository.findById(id).get();
    }

    @Override
    public Player updatePlayer(Long id, Player player) {
        Player updatePlayer = getPlayer(id);

        String name = player.getName();
        if (name != null) {
            checkPlayerName(player);
            updatePlayer.setName(name);
        }

        String title = player.getTitle();
        if (title != null) {
            checkPlayerTitle(player);
            updatePlayer.setTitle(title);
        }

        Race race = player.getRace();
        if (race != null) {
            updatePlayer.setRace(race);
        }

        Profession profession = player.getProfession();
        if (profession != null) {
            updatePlayer.setProfession(profession);
        }

        Date birthday = player.getBirthday();
        if (birthday != null) {
            checkPlayerBirthday(player);
            updatePlayer.setBirthday(birthday);
        }

        Boolean banned = player.getBanned();
        if (banned != null) {
            updatePlayer.setBanned(banned);
        }

        Integer experience = player.getExperience();
        if (experience != null) {
            checkPlayerExperience(player);
            updatePlayer.setExperience(experience);
        }

        updatePlayer.setLevel(calculatePlayerLevel(updatePlayer));
        updatePlayer.setUntilNextLevel(calculatePlayerUntilNextLevel(updatePlayer));
        return playerRepository.save(updatePlayer);
    }

    @Override
    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new NotFoundException();
        }
        playerRepository.deleteById(id);
    }

    @Override
    public Long checkId(String id) {
        if (id == null || id.equals("") || id.equals("0")) {
            throw new BadRequestException();
        }

        Long parseId;

        try {
            parseId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BadRequestException();
        }

        if (parseId < 0) {
            throw new BadRequestException();
        }

        return parseId;
    }

    @Override
    public Specification<Player> selectByName(String name) {
        return ((root, query, criteriaBuilder) -> name == null ? null : criteriaBuilder.like(root.get("name"), "%" + name + "%"));
    }

    @Override
    public Specification<Player> selectByTitle(String title) {
        return ((root, query, criteriaBuilder) -> title == null ? null : criteriaBuilder.like(root.get("title"), "%" + title + "%"));
    }

    @Override
    public Specification<Player> selectByRace(Race race) {
        return ((root, query, criteriaBuilder) -> race == null ? null : criteriaBuilder.equal(root.get("race"), race));
    }

    @Override
    public Specification<Player> selectByProfession(Profession profession) {
        return ((root, query, criteriaBuilder) -> profession == null ? null : criteriaBuilder.equal(root.get("profession"), profession));
    }

    @Override
    public Specification<Player> selectByExperience(Integer minExperience, Integer maxExperience) {
        return (root, query, criteriaBuilder) -> {
            if (minExperience == null && maxExperience == null) {
                return null;
            }
            if (minExperience == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("experience"), maxExperience);
            }
            if (maxExperience == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), minExperience);
            }
            return criteriaBuilder.between(root.get("experience"), minExperience, maxExperience);

        };
    }

    @Override
    public Specification<Player> selectByLevel(Integer minLevel, Integer maxLevel) {
        return (root, query, criteriaBuilder) -> {
            if (minLevel == null && maxLevel == null) {
                return null;
            } else if (minLevel == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("level"), maxLevel);
            } else if (maxLevel == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("level"), minLevel);
            } else {
                return criteriaBuilder.between(root.get("level"), minLevel, maxLevel);
            }
        };
    }

    @Override
    public Specification<Player> selectByBirthday(Long after, Long before) {
        return ((root, query, criteriaBuilder) -> {
            if (after == null && before == null) {
                return null;
            } else if (after == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("birthday"), new Date(before));
            } else if (before == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), new Date(after));
            } else {
                return criteriaBuilder.between(root.get("birthday"), new Date(after), new Date(before));
            }
        });
    }

    @Override
    public Specification<Player> selectByBanned(Boolean banned) {
        return ((root, query, criteriaBuilder) -> {
            if (banned == null) {
                return null;
            } else {
                return banned ? criteriaBuilder.isTrue(root.get("banned")) : criteriaBuilder.isFalse(root.get("banned"));
            }
        });
    }

    private void checkPlayerName(Player player) {
        String name = player.getName();
        if (name.length() < 1 || name.length() > 12) {
            throw new BadRequestException();
        }
    }

    private void checkPlayerTitle(Player player) {
        String title = player.getTitle();
        if (title.length() < 1 || title.length() > 30) {
            throw new BadRequestException();
        }
    }

    private void checkPlayerExperience(Player player) {
        Integer experience = player.getExperience();
        if (experience < 0 || experience > 10_000_000) {
            throw new BadRequestException();
        }
    }

    private void checkPlayerBirthday(Player player) {
        Date birthDay = player.getBirthday();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(birthDay);
        int year = calendar.get(Calendar.YEAR);
        if (year < 2000 || year > 3000) {
            throw new BadRequestException();
        }
    }

    private Integer calculatePlayerLevel(Player player) {
        Integer experience = player.getExperience();
        return (Integer) (int) ((Math.sqrt(2500 + 200 * experience) - 50) / 100);
    }

    private Integer calculatePlayerUntilNextLevel(Player player) {
        Integer experience = player.getExperience();
        Integer level = player.getLevel();
        return (50 * (level + 1) * (level + 2) - experience);
    }
}
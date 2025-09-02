package service;

import dn.quest.model.dto.TeamDTO;
import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.TeamMemberRepository;
import dn.quest.repositories.TeamRepository;
import dn.quest.repositories.UserRepository;
import dn.quest.services.impl.TeamServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamServiceImplTest {

    @Mock private TeamRepository teamRepository;
    @Mock private UserRepository userRepository;
    @Mock private TeamMemberRepository teamMemberRepository;

    @InjectMocks private TeamServiceImpl teamService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Creating a team, adding a player and capitan")
    void createTeam_setsCaptainAndMember() {
        User captain = new User();
        captain.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(captain));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> {
            Team t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        TeamDTO team = teamService.createTeam("Testers", Long.valueOf(1));

        assertNotNull(team.getId());
        assertEquals("Testers", team.getName());
        assertEquals(captain, team.getCaptain());

        verify(teamMemberRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Get by id not found")
    void getById_notFound_throws() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());
assertNull(teamService.getById(99L));
        assertThrows(Exception.class, () -> teamService.getById(99L));
    }
}
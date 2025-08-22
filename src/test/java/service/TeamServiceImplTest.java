package service;


import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.TeamMemberRepository;
import dn.quest.repositories.TeamRepository;
import dn.quest.repositories.UserRepository;
import dn.quest.services.TeamServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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
    void createTeam_setsCaptainAndMember() {
        User captain = new User();
        captain.setId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(captain));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> {
            Team t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        Team team = teamService.createTeam("Testers", 1);

        assertNotNull(team.getId());
        assertEquals("Testers", team.getName());
        assertEquals(captain, team.getCaptain());

        verify(teamMemberRepository, times(1)).save(any());
    }

    @Test
    void getById_notFound_throws() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> teamService.getById(99L));
    }
}
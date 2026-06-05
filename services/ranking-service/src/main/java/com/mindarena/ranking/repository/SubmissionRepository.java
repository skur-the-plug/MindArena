package com.mindarena.ranking.repository;

import com.mindarena.ranking.dto.LeaderboardEntry;
import com.mindarena.ranking.model.Arena;
import com.mindarena.ranking.model.Challenge;
import com.mindarena.ranking.model.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    @Query("""
            select new com.mindarena.ranking.dto.LeaderboardEntry(
                submission.author,
                count(submission) * 10
                    + coalesce(sum(submission.upvotes * case submission.challenge.difficulty
                        when 'Beginner' then 3
                        when 'Advanced' then 8
                        when 'Expert' then 12
                        else 5
                    end), 0)
                    + sum(case when submission.bestAnswer = true then case submission.challenge.difficulty
                        when 'Beginner' then 15
                        when 'Advanced' then 40
                        when 'Expert' then 60
                        else 25
                    end else 0 end)
            )
            from Submission submission
            where submission.author.role = com.mindarena.ranking.model.Role.USER
            and submission.challenge.arena = :arena
            group by submission.author
            order by count(submission) * 10
                + coalesce(sum(submission.upvotes * case submission.challenge.difficulty
                    when 'Beginner' then 3
                    when 'Advanced' then 8
                    when 'Expert' then 12
                    else 5
                end), 0)
                + sum(case when submission.bestAnswer = true then case submission.challenge.difficulty
                    when 'Beginner' then 15
                    when 'Advanced' then 40
                    when 'Expert' then 60
                    else 25
                end else 0 end) desc
            """)
    List<LeaderboardEntry> rankByArena(Arena arena);

    @Query("""
            select new com.mindarena.ranking.dto.LeaderboardEntry(
                submission.author,
                count(submission) * 10
                    + coalesce(sum(submission.upvotes * case submission.challenge.difficulty
                        when 'Beginner' then 3
                        when 'Advanced' then 8
                        when 'Expert' then 12
                        else 5
                    end), 0)
                    + sum(case when submission.bestAnswer = true then case submission.challenge.difficulty
                        when 'Beginner' then 15
                        when 'Advanced' then 40
                        when 'Expert' then 60
                        else 25
                    end else 0 end)
            )
            from Submission submission
            where submission.author.role = com.mindarena.ranking.model.Role.USER
            and submission.challenge = :challenge
            group by submission.author
            order by count(submission) * 10
                + coalesce(sum(submission.upvotes * case submission.challenge.difficulty
                    when 'Beginner' then 3
                    when 'Advanced' then 8
                    when 'Expert' then 12
                    else 5
                end), 0)
                + sum(case when submission.bestAnswer = true then case submission.challenge.difficulty
                    when 'Beginner' then 15
                    when 'Advanced' then 40
                    when 'Expert' then 60
                    else 25
                end else 0 end) desc
            """)
    List<LeaderboardEntry> rankByChallenge(Challenge challenge);
}

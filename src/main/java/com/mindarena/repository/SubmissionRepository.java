package com.mindarena.repository;

import com.mindarena.dto.LeaderboardEntry;
import com.mindarena.model.Arena;
import com.mindarena.model.Challenge;
import com.mindarena.model.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByChallengeOrderByUpvotesDescCreatedAtAsc(Challenge challenge);

    List<Submission> findByChallengeAndBestAnswerTrue(Challenge challenge);

    @Query("""
            select new com.mindarena.dto.LeaderboardEntry(
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
            where submission.author.role = com.mindarena.model.Role.USER
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
            select new com.mindarena.dto.LeaderboardEntry(
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
            where submission.author.role = com.mindarena.model.Role.USER
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

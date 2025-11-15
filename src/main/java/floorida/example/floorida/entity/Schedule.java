package floorida.example.floorida.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "creator_user_id", nullable = false)
    private Long creatorUserId;

    @Column(name = "team_id")
    private Long teamId; // nullable: 개인 일정 가능

    @Column(nullable = false, length = 255)
    private String title;

    // 사용자가 AI 생성 시 입력한 원래 목표 텍스트 (수동 생성 일정은 null 가능)
    @Column(name = "original_goal", length = 1000)
    private String originalGoal;

    // 목표에 대한 요약/설명 (AI가 생성하거나 사용자가 직접 기술, 선택 사항)
    @Column(name = "goal_summary", length = 2000)
    private String goalSummary;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 7)
    private String color;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FloorPlan> floors = new ArrayList<>();

    public void addFloor(FloorPlan floor) {
        floor.setSchedule(this);
        this.floors.add(floor);
    }
}

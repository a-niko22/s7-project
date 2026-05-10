package s7project.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChannelJpaRepository extends JpaRepository<ChannelEntity, String> {
    List<ChannelEntity> findAllByOrderBySortOrderAsc();
}

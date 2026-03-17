package dn.quest.authentication.repository;

import dn.quest.authentication.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с разрешениями
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // Базовые запросы поиска
    Optional<Permission> findByName(String name);
    List<Permission> findByCategory(String category);
    List<Permission> findByNameContainingIgnoreCase(String name);

    // Поиск с пагинацией
    @Query("SELECT p FROM Permission p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Permission> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT p FROM Permission p WHERE p.category = :category")
    Page<Permission> findByCategoryPaged(@Param("category") String category, Pageable pageable);

    // Получение всех категорий
    @Query("SELECT DISTINCT p.category FROM Permission p ORDER BY p.category")
    List<String> findAllCategories();

    // Поиск по нескольким именам
    @Query("SELECT p FROM Permission p WHERE p.name IN :names")
    List<Permission> findByNameIn(@Param("names") List<String> names);

    // Проверка существования
    boolean existsByName(String name);

    // Поиск по категории и имени
    @Query("SELECT p FROM Permission p WHERE p.category = :category AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Permission> findByCategoryAndNameContainingIgnoreCase(@Param("category") String category, @Param("name") String name);

    // Получение разрешений для категории
    @Query("SELECT p FROM Permission p WHERE p.category = :category ORDER BY p.name")
    List<Permission> findByCategoryOrderByName(@Param("category") String category);
}
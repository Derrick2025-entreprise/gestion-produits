package com.eadl.gestionproduits.config;

import com.eadl.gestionproduits.entity.Category;
import com.eadl.gestionproduits.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initCategories(CategoryRepository categoryRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                List.of("Informatique", "Téléphonie", "Électroménager")
                        .forEach(name -> categoryRepository.save(new Category(name)));
                log.info("Catégories initiales créées");
            }
        };
    }
}

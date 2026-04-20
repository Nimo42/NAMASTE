package com.ayush.terminology.namaste;

import com.ayush.terminology.namaste.mappings.Mapping;
import com.ayush.terminology.namaste.model.AppUser;
import com.ayush.terminology.namaste.model.CodeSystem;
import com.ayush.terminology.namaste.model.Concept;
import com.ayush.terminology.namaste.model.Role;
import com.ayush.terminology.namaste.repo.CodeSystemRepository;
import com.ayush.terminology.namaste.repo.ConceptRepository;
import com.ayush.terminology.namaste.repo.MappingRepository;
import com.ayush.terminology.namaste.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class NamasteApplication {

	public static void main(String[] args) {
		SpringApplication.run(NamasteApplication.class, args);
	}

	/**
	 * Seeds default data on first run. Skips if data already exists (file-based H2 persists).
	 */
	@Bean
	CommandLineRunner init(UserRepository userRepo, PasswordEncoder encoder,
						   CodeSystemRepository csRepo, ConceptRepository conceptRepo,
						   MappingRepository mappingRepo) {
		return args -> {
			// 1. Admin user
			if (userRepo.findByEmail("admin@namaste.local").isEmpty()) {
				AppUser admin = AppUser.builder()
						.name("Admin")
						.email("admin@namaste.local")
						.password(encoder.encode("admin123"))
						.role(Role.ADMIN)
						.enabled(true)
						.registrationStatus("APPROVED")
						.build();
				userRepo.save(admin);
				System.out.println(">>> Default admin user created: admin@namaste.local / admin123");
			}

			// 2. Code Systems (only seed if none exist)
			if (csRepo.count() == 0) {
				CodeSystem namaste = csRepo.save(CodeSystem.builder()
						.name("NAMASTE").displayName("NAMASTE").version("1.0")
						.type("LOCAL").isActive(true).build());

				CodeSystem icdTm2 = csRepo.save(CodeSystem.builder()
						.name("ICD11_TM2").displayName("ICD-11 TM2").version("2025-06")
						.type("AUTHORITY").isActive(true).build());

				CodeSystem icdBiomed = csRepo.save(CodeSystem.builder()
						.name("ICD11_BIOMED").displayName("ICD-11 Biomedical").version("2025-06")
						.type("AUTHORITY").isActive(true).build());

				// 3. Concepts
				Concept amaJwara = conceptRepo.save(Concept.builder()
						.code("N123").displayName("Ama Jwara")
						.description("Fever due to impaired digestion")
						.codeSystem(namaste).isActive(true).lifecycleStatus("ACTIVE").build());

				Concept madhumeha = conceptRepo.save(Concept.builder()
						.code("N456").displayName("Madhumeha")
						.description("Sweet urination disorder")
						.codeSystem(namaste).isActive(true).lifecycleStatus("ACTIVE").build());

				Concept feverPattern = conceptRepo.save(Concept.builder()
						.code("TM2-789").displayName("Fever Pattern")
						.description("Traditional fever concept")
						.codeSystem(icdTm2).isActive(true).lifecycleStatus("ACTIVE").build());

				Concept metabolicPattern = conceptRepo.save(Concept.builder()
						.code("TM2-999").displayName("Metabolic Disorder Pattern")
						.description("Sugar disorder pattern")
						.codeSystem(icdTm2).isActive(true).lifecycleStatus("ACTIVE").build());

				Concept diabetes = conceptRepo.save(Concept.builder()
						.code("5A11").displayName("Type 2 Diabetes Mellitus")
						.description("Modern diabetes classification")
						.codeSystem(icdBiomed).isActive(true).lifecycleStatus("ACTIVE").build());

				Concept fever = conceptRepo.save(Concept.builder()
						.code("AA12").displayName("Fever")
						.description("Fever in modern medicine")
						.codeSystem(icdBiomed).isActive(true).lifecycleStatus("ACTIVE").build());

				// 4. Mappings
				mappingRepo.save(Mapping.builder()
						.fromConcept(madhumeha).toConcept(diabetes)
						.mappingType("EQUIVALENT").confidence(0.95)
						.status("VERIFIED").build());

				mappingRepo.save(Mapping.builder()
						.fromConcept(amaJwara).toConcept(fever)
						.mappingType("EQUIVALENT").confidence(0.90)
						.status("VERIFIED").build());

				System.out.println(">>> Seed data loaded: 3 code systems, 6 concepts, 2 mappings");
			} else {
				System.out.println(">>> Data already exists in DB — skipping seed");
			}
		};
	}
}

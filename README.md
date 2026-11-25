# API REST — Gestion de produits

Projet EC 1 (EADL 4, CC 1). API REST qui gère le catalogue de produits d'une boutique : produits, catégories et stock.

Architecture en couches (Controller, Service, Repository), DTO, exceptions métier avec gestionnaire global, tests automatisés (JUnit 5, Mockito, MockMvc, `@DataJpaTest`), couverture JaCoCo, analyse SonarQube et documentation Swagger (OpenAPI).

## Technologies

| Élément | Version |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.0.10 |
| springdoc-openapi | 3.0.3 |
| Base de données | MySQL 8 (H2 en mémoire pour les tests) |
| Couverture | JaCoCo 0.8.13 |

## Prérequis

- JDK 21
- Un serveur MySQL sur `localhost:3306` (XAMPP ou Docker). La base `gestion_produits_db` est créée automatiquement au premier démarrage.
- Maven : inutile de l'installer, le projet contient le Maven Wrapper (`mvnw`).

## Configuration

Les identifiants MySQL se lisent dans des variables d'environnement. Valeurs par défaut : utilisateur `root`, mot de passe vide (configuration XAMPP).

| Variable | Défaut |
| --- | --- |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | (vide) |

Avec MySQL dans Docker :

```bash
docker run -d --name mysql-produits -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=gestion_produits_db mysql:8
```

puis lancer l'application avec `DB_PASSWORD=root`.

## Lancer l'application

```bash
./mvnw spring-boot:run          # Linux / macOS
mvnw.cmd spring-boot:run        # Windows
```

Au premier démarrage, trois catégories sont créées : Informatique (id 1), Téléphonie, Électroménager.

| Ressource | Adresse |
| --- | --- |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Description OpenAPI (JSON) | http://localhost:8080/v3/api-docs |

## Lancer les tests et la couverture

```bash
./mvnw clean verify
```

Les tests utilisent H2 : MySQL n'est pas nécessaire. Le rapport de couverture est dans `target/site/jacoco/index.html`. Le build échoue si la couverture descend sous 80 % au total ou sous 90 % sur le package `service`.

## Analyse SonarQube

```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:community
./mvnw clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=<JETON>
```

## Endpoints

Toutes les URI commencent par `/api/v1`.

| Verbe | URI | Action | Statuts |
| --- | --- | --- | --- |
| POST | /products | Créer un produit | 201, 400, 404, 409 |
| GET | /products | Lister (page, size, sort, name, categoryId, minPrice, maxPrice) | 200, 400 |
| GET | /products/{id} | Consulter un produit | 200, 400, 404 |
| PUT | /products/{id} | Modifier un produit | 200, 400, 404, 409 |
| PATCH | /products/{id}/stock | Ajuster le stock (`{"delta": -3}`) | 200, 400, 404, 409 |
| DELETE | /products/{id} | Supprimer un produit | 204, 404 |
| POST | /categories | Créer une catégorie | 201, 400, 409 |
| GET | /categories | Lister les catégories | 200 |

Toutes les erreurs ont le même format (`ApiError`) :

```json
{
  "timestamp": "2026-11-23T09:35:12Z",
  "status": 404,
  "code": "PRODUCT_NOT_FOUND",
  "message": "Produit introuvable : id=99",
  "path": "/api/v1/products/99",
  "violations": []
}
```

## Organisation du code

```
com.eadl.gestionproduits
├── config/       OpenApiConfig, DataInitializer
├── controller/   ProductController, CategoryController, SortParser
├── service/      ProductService, CategoryService
├── repository/   ProductRepository, CategoryRepository
├── entity/       Product, Category, ProductLimits
├── dto/          ProductRequest, ProductResponse, StockAdjustmentRequest, CategoryRequest,
│                 CategoryResponse, PageResponse, ProductFilter, ApiError, FieldViolation
├── mapper/       ProductMapper, CategoryMapper
└── exception/    BusinessException et ses sous-classes, ErrorCodes, GlobalExceptionHandler
```

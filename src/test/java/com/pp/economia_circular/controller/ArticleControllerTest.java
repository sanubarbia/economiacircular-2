package com.pp.economia_circular.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pp.economia_circular.DTO.ArticleCreateDto;
import com.pp.economia_circular.DTO.ArticleResponseDto;
import com.pp.economia_circular.DTO.ArticleSearchDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.EventRepository;
import com.pp.economia_circular.repositories.MensajeRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.ArticleService;
import com.pp.economia_circular.service.EventService;
import com.pp.economia_circular.service.JWTService;
import com.pp.economia_circular.service.ServicioMensaje;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ArticleController.class)
@org.springframework.context.annotation.Import(com.pp.economia_circular.config.TestSecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private ServicioMensaje servicioMensaje;

    @MockBean
    private MensajeRepository mensajeRepository;

    @MockBean
    private EventService eventService;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private com.pp.economia_circular.service.RecyclingCenterService recyclingCenterService;

    @MockBean
    private com.pp.economia_circular.repositories.RecyclingCenterRepository recyclingCenterRepository;

    @MockBean
    private com.pp.economia_circular.repositories.TallerRepository tallerRepository;

    @MockBean
    private com.pp.economia_circular.service.ReportService reportService;

    @MockBean
    private com.pp.economia_circular.service.EmailService emailService;

    private ArticleCreateDto createDto;
    private ArticleResponseDto responseDto;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        createDto = new ArticleCreateDto();
        createDto.setTitle("Test Article");
        createDto.setDescription("Test Description");
        createDto.setCategory(Articulo.CategoriaArticulo.ELECTRONICOS);
        createDto.setCondition(Articulo.CondicionArticulo.USADO);

        responseDto = ArticleResponseDto.builder()
                .id(1L)
                .title("Test Article")
                .description("Test Description")
                .category(Articulo.CategoriaArticulo.ELECTRONICOS)
                .condition(Articulo.CondicionArticulo.USADO)
                .status(Articulo.EstadoArticulo.DISPONIBLE)
                .userId(1L)
                .username("test@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    void createArticle_Success() throws Exception {
        // Arrange
        when(articleService.createArticle(any(ArticleCreateDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/articles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Article"))
                .andExpect(jsonPath("$.category").value("ELECTRONICOS"));

        verify(articleService, times(1)).createArticle(any(ArticleCreateDto.class));
    }

    @Test
    void createArticle_Unauthorized() throws Exception {
        // Act & Assert - sin autenticación
        mockMvc.perform(post("/api/articles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());

        verify(articleService, never()).createArticle(any());
    }

    @Test
    void getAllArticles_Success() throws Exception {
        // Arrange
        Page<ArticleResponseDto> page = new PageImpl<>(Arrays.asList(responseDto));
        when(articleService.getAllArticles(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/articles")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Article"));
    }

    @Test
    void searchArticles_Success() throws Exception {
        // Arrange
        Page<ArticleResponseDto> page = new PageImpl<>(Arrays.asList(responseDto));
        when(articleService.searchArticles(any(ArticleSearchDto.class), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/articles/search")
                .param("title", "Test")
                .param("category", "ELECTRONICOS")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Article"));
    }

    @Test
    void getArticlesByCategory_Success() throws Exception {
        // Arrange
        when(articleService.getArticlesByCategory(Articulo.CategoriaArticulo.ELECTRONICOS))
            .thenReturn(Arrays.asList(responseDto));

        // Act & Assert
        mockMvc.perform(get("/api/articles/category/ELECTRONICOS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("ELECTRONICOS"));
    }

    @Test
    void getMostViewedArticles_Success() throws Exception {
        // Arrange
        when(articleService.getMostViewedArticles(any())).thenReturn(Arrays.asList(responseDto));

        // Act & Assert
        mockMvc.perform(get("/api/articles/most-viewed")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Article"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyArticles_Success() throws Exception {
        // Arrange
        when(articleService.getMyArticles()).thenReturn(Arrays.asList(responseDto));

        // Act & Assert
        mockMvc.perform(get("/api/articles/my-articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Article"));
    }

    @Test
    void getMyArticles_Unauthorized() throws Exception {
        // Act & Assert - sin autenticación
        mockMvc.perform(get("/api/articles/my-articles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getArticlesByUser_Success() throws Exception {
        // Arrange
        when(articleService.getArticlesByUser(1L)).thenReturn(Arrays.asList(responseDto));

        // Act & Assert
        mockMvc.perform(get("/api/articles/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L));
    }

    @Test
    void getArticleById_Success() throws Exception {
        // Arrange
        when(articleService.getArticleById(1L)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Article"));
    }

    @Test
    void getArticleById_NotFound() throws Exception {
        // Arrange
        when(articleService.getArticleById(999L))
            .thenThrow(new RuntimeException("Artículo no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/articles/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Artículo no encontrado"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateArticle_Success() throws Exception {
        // Arrange
        when(articleService.updateArticle(anyLong(), any(ArticleCreateDto.class)))
            .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/articles/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(articleService, times(1)).updateArticle(anyLong(), any(ArticleCreateDto.class));
    }

    @Test
    void updateArticle_Unauthorized() throws Exception {
        // Act & Assert - sin autenticación
        mockMvc.perform(put("/api/articles/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteArticle_Success() throws Exception {
        // Arrange
        doNothing().when(articleService).deleteArticle(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/articles/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Artículo eliminado exitosamente"));

        verify(articleService, times(1)).deleteArticle(1L);
    }

    @Test
    void deleteArticle_Unauthorized() throws Exception {
        // Act & Assert - sin autenticación
        mockMvc.perform(delete("/api/articles/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteArticle_NotOwner() throws Exception {
        // Arrange
        doThrow(new RuntimeException("No tienes permisos para eliminar este artículo"))
            .when(articleService).deleteArticle(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/articles/1")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No tienes permisos para eliminar este artículo"));
    }
}


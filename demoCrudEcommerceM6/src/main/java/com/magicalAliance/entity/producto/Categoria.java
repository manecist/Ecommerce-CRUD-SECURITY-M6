package com.magicalAliance.entity.producto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCategoria")
    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Column(name = "nombreCategoria", nullable = false, length = 100)
    private String nombre;

    @Column(name = "imagenBanner")
    private String imagenBanner = "default-banner.jpg";

    // Una categoría tiene muchas subcategorías
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subcategoria> subcategorias;
}
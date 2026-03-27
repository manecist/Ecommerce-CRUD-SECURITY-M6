package com.magicalAliance.entity.producto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "subcategorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subcategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSubcategoria")
    private Long id;

    @NotBlank(message = "El nombre de la subcategoría es obligatorio")
    @Column(name = "nombreSubcategoria", nullable = false, length = 100)
    private String nombre;

    // Relación con Categoría
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idCategoriaAsociada", nullable = false)
    private Categoria categoria;

    // Una subcategoría tiene muchos productos
    @OneToMany(mappedBy = "subcategoria", cascade = CascadeType.ALL)
    private List<Producto> productos;
}
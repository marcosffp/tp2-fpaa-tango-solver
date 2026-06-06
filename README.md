# Tango Puzzle Solver

**TP2 — Fundamentos de Projeto e Análise de Algoritmos · PUC Minas 2026/1**

Resolução do quebra-cabeça Tango com **Força Bruta** e **Backtracking** em Java.
Implementação com arquitetura de duas camadas: motor CSP genérico separado da lógica do domínio Tango.

**Autores:** Bernardo Alvim, Gabriela Alvarenga, Marcos Alberto, Mateus Araujo, Pedro Seabra

---

## Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Java (JDK) | 17 |
| Maven | 3.6 |

---

## Compilar

```bash
cd code
mvn package -DskipTests
```

---

## Rodar

> Os comandos abaixo devem ser executados a partir do diretório `code/`.

### Executar todos os tabuleiros (BT + BF)

```bash
java -cp target/classes tp2.fpaa.Main
```

### Escolher o algoritmo para todos os tabuleiros

```bash
java -cp target/classes tp2.fpaa.Main bt    # só Backtracking
java -cp target/classes tp2.fpaa.Main bf    # só Força Bruta
java -cp target/classes tp2.fpaa.Main both  # ambos (padrão)
```

### Executar um tabuleiro específico

```bash
java -cp target/classes tp2.fpaa.Main <tabuleiro> <algoritmo>
```

**Tabuleiros disponíveis:**

| Nome | Atalho | Tamanho | Dificuldade |
|---|---|---|---|
| `4x4` | `4x4` | 4×4 | — |
| `6x6-easy` | `easy` | 6×6 | Fácil |
| `6x6-medium` | `medium` | 6×6 | Médio |
| `6x6-hard` | `hard` | 6×6 | Difícil |
| `8x8` | `8x8` | 8×8 | — |
| `16x16` | `16x16` | 16×16 | — |

**Algoritmos:** `bt` (Backtracking) · `bf` (Força Bruta) · `both` (ambos)

**Exemplos:**

```bash
# 4×4 com ambos os algoritmos
java -cp target/classes tp2.fpaa.Main 4x4 both

# 6×6 difícil só com Backtracking
java -cp target/classes tp2.fpaa.Main hard bt

# 16×16 (Força Bruta é ignorada automaticamente)
java -cp target/classes tp2.fpaa.Main 16x16 bt
```

> Tabuleiros maiores que 6×6 ignoram automaticamente a Força Bruta e exibem um aviso com o tamanho do espaço de busca.

---

## Estrutura do Projeto

```
code/
├── pom.xml
└── src/main/
    ├── java/tp2/fpaa/
    │   ├── csp/                        ← Motor genérico de busca (independente do Tango)
    │   │   ├── contract/               ← State, ConstraintChecker, VariableSelector, ValueIterator
    │   │   ├── engine/                 ← BacktrackingEngine, BruteForceEngine
    │   │   └── result/                 ← SolveResult
    │   ├── tango/                      ← Lógica específica do quebra-cabeça
    │   │   ├── domain/                 ← Symbol, ConstraintType, Constraint
    │   │   ├── board/                  ← Cell, Board, BoardFactory
    │   │   ├── validation/             ← Rule, BalanceRule, ConsecutiveRule, EqualRule,
    │   │   │                               OppositionRule, TangoConstraintChecker
    │   │   ├── heuristic/              ← TangoVariableSelector, TangoValueIterator
    │   │   ├── adapter/                ← TangoBoardAdapter (ponte entre as duas camadas)
    │   │   └── io/                     ← BoardParser, BoardPrinter, ResultPrinter, RunResult
    │   └── Main.java
    └── resources/                      ← board_4x4.txt, board_6x6_easy.txt, board_6x6_medium.txt,
                                            board_6x6_hard.txt, board_8x8.txt, board_16x16.txt
```

---

## Saída esperada

```
╔═════════════════════════════╗
║  ☀  TANGO PUZZLE SOLVER  ☽  ║
╚═════════════════════════════╝

  Executando todos os boards...

┌─ Board: 4x4 ───────────────────────────────┐

══ Backtracking ════════════════════

─── Solução ──────────────────────────────
  ┌───┬───┬───┬───┐
  │ ☀ = ☀ × ☽ = ☽ │
  ├─×─┼───┼───┼───┤
  │ ☽ = ☽ × ☀ = ☀ │
  ├───┼───┼───┼───┤
  │ ☽ │ ☀ = ☀ │ ☽ │
  ├───┼───┼───┼───┤
  │ ☀ │ ☽ │ ☽ │ ☀ │
  └───┴───┴───┴───┘

  ✓ Solução encontrada!
    Tempo                    : 1,90 ms
    Nós visitados            : 15
    Verificações de restrição: 23
    Podas (restrição violada): 9
```

---

## Regras do Tango

1. **Preenchimento completo** — toda célula recebe Sol (☀) ou Lua (☽)
2. **Limite de adjacência** — máximo 2 símbolos iguais consecutivos em linha ou coluna
3. **Equilíbrio** — cada linha e coluna tem exatamente N/2 Sóis e N/2 Luas
4. **Restrição `=`** — células conectadas por `=` devem ter o mesmo símbolo
5. **Restrição `×`** — células conectadas por `×` devem ter símbolos opostos

from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.units import cm
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_LEFT, TA_CENTER, TA_JUSTIFY
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, Table,
                                 TableStyle, HRFlowable, PageBreak, KeepTogether)
from reportlab.platypus.flowables import Flowable
import re
import html as _html

def _esc(text):
    """Escape HTML special chars so ReportLab's XML parser doesn't choke."""
    return _html.escape(str(text))

# ─── COLORS ────────────────────────────────────────────────────────────────────
C_BG_DARK    = colors.HexColor("#09090b")
C_NAVY       = colors.HexColor("#0f1123")
C_PURPLE     = colors.HexColor("#6366f1")
C_BLUE       = colors.HexColor("#3b82f6")
C_GREEN      = colors.HexColor("#10b981")
C_YELLOW     = colors.HexColor("#f59e0b")
C_RED        = colors.HexColor("#ef4444")
C_CYAN       = colors.HexColor("#06b6d4")

C_WHY_BG     = colors.HexColor("#0d1f3c")
C_WHY_BORDER = colors.HexColor("#3b82f6")
C_EX_BG      = colors.HexColor("#0d2818")
C_EX_BORDER  = colors.HexColor("#10b981")
C_CODE_BG    = colors.HexColor("#1a1a2e")
C_TIP_BG     = colors.HexColor("#1f1a00")
C_TIP_BORDER = colors.HexColor("#f59e0b")

C_TEXT       = colors.HexColor("#e2e8f0")
C_MUTED      = colors.HexColor("#94a3b8")
C_H1         = colors.HexColor("#c7d2fe")
C_H2         = colors.HexColor("#a5b4fc")
C_H3         = colors.HexColor("#93c5fd")
C_H4         = colors.HexColor("#6ee7b7")
C_CODE_TEXT  = colors.HexColor("#a5f3fc")
C_PAGE_BG    = colors.HexColor("#09090b")
C_TABLE_HDR  = colors.HexColor("#1e1b4b")
C_TABLE_ROW  = colors.HexColor("#0f0f1e")
C_TABLE_ALT  = colors.HexColor("#13132a")

PAGE_W, PAGE_H = A4

# ─── DOCUMENT SETUP ────────────────────────────────────────────────────────────
def build_doc(output_path):
    doc = SimpleDocTemplate(
        output_path,
        pagesize=A4,
        leftMargin=1.8*cm,
        rightMargin=1.8*cm,
        topMargin=2.2*cm,
        bottomMargin=2.2*cm,
        title="ServeFlow — Manual do Sistema e Arquitetura",
        author="Equipe ServeFlow",
        subject="Arquitetura, Tecnologias e Guia do Usuário"
    )

    styles = make_styles()
    story = []

    # Cover
    story += make_cover(styles)
    story.append(PageBreak())

    # Content
    story += make_body(styles)

    def on_page(canvas, doc):
        draw_page(canvas, doc)

    doc.build(story, onFirstPage=on_page, onLaterPages=on_page)
    print(f"PDF gerado: {output_path}")

# ─── PAGE BACKGROUND ───────────────────────────────────────────────────────────
def draw_page(canvas, doc):
    canvas.saveState()
    canvas.setFillColor(C_PAGE_BG)
    canvas.rect(0, 0, PAGE_W, PAGE_H, fill=1, stroke=0)

    if doc.page > 1:
        # Header line
        canvas.setStrokeColor(C_PURPLE)
        canvas.setLineWidth(1)
        canvas.line(1.8*cm, PAGE_H - 1.4*cm, PAGE_W - 1.8*cm, PAGE_H - 1.4*cm)
        # Header text
        canvas.setFont("Helvetica", 7)
        canvas.setFillColor(C_MUTED)
        canvas.drawString(1.8*cm, PAGE_H - 1.2*cm, "ServeFlow — Manual do Sistema e Arquitetura")
        canvas.drawRightString(PAGE_W - 1.8*cm, PAGE_H - 1.2*cm, f"Página {doc.page}")
        # Footer line
        canvas.setStrokeColor(C_PURPLE)
        canvas.line(1.8*cm, 1.6*cm, PAGE_W - 1.8*cm, 1.6*cm)
        canvas.setFont("Helvetica", 7)
        canvas.setFillColor(C_MUTED)
        canvas.drawString(1.8*cm, 1.2*cm, "Spring Boot 3.4.3 / Java 21 / PostgreSQL / React / Vercel / Render")
        canvas.drawRightString(PAGE_W - 1.8*cm, 1.2*cm, "Maio 2026  |  v2.0")

    canvas.restoreState()

# ─── STYLES ────────────────────────────────────────────────────────────────────
def make_styles():
    base = getSampleStyleSheet()

    def s(name, **kw):
        return ParagraphStyle(name, **kw)

    return {
        "cover_title": s("ct", fontSize=30, textColor=C_H1, fontName="Helvetica-Bold",
                          alignment=TA_CENTER, spaceAfter=8, leading=36),
        "cover_sub":   s("cs", fontSize=13, textColor=C_PURPLE, fontName="Helvetica",
                          alignment=TA_CENTER, spaceAfter=6, leading=18),
        "cover_meta":  s("cm", fontSize=10, textColor=C_MUTED, fontName="Helvetica",
                          alignment=TA_CENTER, spaceAfter=4, leading=14),

        "h1": s("h1", fontSize=18, textColor=C_H1, fontName="Helvetica-Bold",
                 spaceBefore=18, spaceAfter=6, leading=24,
                 borderPad=4),
        "h2": s("h2", fontSize=14, textColor=C_H2, fontName="Helvetica-Bold",
                 spaceBefore=14, spaceAfter=4, leading=20),
        "h3": s("h3", fontSize=12, textColor=C_H3, fontName="Helvetica-Bold",
                 spaceBefore=10, spaceAfter=4, leading=18),
        "h4": s("h4", fontSize=11, textColor=C_H4, fontName="Helvetica-Bold",
                 spaceBefore=8, spaceAfter=3, leading=16),

        "body": s("body", fontSize=10, textColor=C_TEXT, fontName="Helvetica",
                   leading=15, spaceAfter=5, alignment=TA_JUSTIFY),
        "bullet": s("bullet", fontSize=10, textColor=C_TEXT, fontName="Helvetica",
                     leading=14, spaceAfter=3, leftIndent=14, bulletIndent=4),
        "code": s("code", fontSize=9, textColor=C_CODE_TEXT, fontName="Courier",
                   leading=13, spaceAfter=2, leftIndent=8),
        "label_why": s("lw", fontSize=10, textColor=C_BLUE, fontName="Helvetica-Bold",
                        leading=14, spaceAfter=2),
        "label_ex":  s("le", fontSize=10, textColor=C_GREEN, fontName="Helvetica-Bold",
                        leading=14, spaceAfter=2),
        "label_tip": s("lt", fontSize=10, textColor=C_YELLOW, fontName="Helvetica-Bold",
                        leading=14, spaceAfter=2),
        "why_body":  s("wb", fontSize=10, textColor=C_TEXT, fontName="Helvetica",
                        leading=14, spaceAfter=2),
        "mono_small":s("ms", fontSize=8.5, textColor=C_CODE_TEXT, fontName="Courier",
                        leading=12, spaceAfter=1),
        "muted": s("mu", fontSize=9, textColor=C_MUTED, fontName="Helvetica",
                    leading=12, spaceAfter=3),
        "toc_h1": s("th1", fontSize=11, textColor=C_H2, fontName="Helvetica-Bold",
                     leading=16, spaceAfter=2, leftIndent=0),
        "toc_h2": s("th2", fontSize=10, textColor=C_TEXT, fontName="Helvetica",
                     leading=14, spaceAfter=1, leftIndent=12),
        "section_num": s("sn", fontSize=10, textColor=C_PURPLE, fontName="Helvetica-Bold",
                          leading=14),
    }

# ─── COVER PAGE ────────────────────────────────────────────────────────────────
def make_cover(styles):
    el = []
    el.append(Spacer(1, 3*cm))

    # Logo bar
    el.append(HRFlowable(width="100%", thickness=3, color=C_PURPLE, spaceAfter=20))

    el.append(Paragraph("ServeFlow", styles["cover_title"]))
    el.append(Paragraph("Manual do Sistema e Arquitetura", styles["cover_sub"]))
    el.append(Spacer(1, 0.5*cm))
    el.append(HRFlowable(width="60%", thickness=1, color=C_PURPLE, spaceAfter=20))
    el.append(Spacer(1, 0.5*cm))

    el.append(Paragraph("Guia completo de estudo: tecnologias, motivações, exemplos práticos e uso do sistema", styles["cover_meta"]))
    el.append(Spacer(1, 2*cm))

    # Stack table
    stack_data = [
        ["Componente", "Tecnologia", "Plataforma"],
        ["Frontend",   "React 18 + TailwindCSS + Axios",     "Vercel"],
        ["Backend",    "Spring Boot 3.4.3 / Java 21",        "Render"],
        ["Banco",      "PostgreSQL 15 + Flyway",              "Supabase"],
        ["Auth",       "JWT (JJWT 0.12.3) + BCrypt",         "—"],
        ["Real-time",  "WebSocket + STOMP",                   "—"],
        ["E-mail",     "Gmail SMTP + STARTTLS",               "—"],
        ["CEP",        "ViaCEP REST API",                     "—"],
    ]
    ts = TableStyle([
        ("BACKGROUND",   (0,0), (-1,0), C_TABLE_HDR),
        ("TEXTCOLOR",    (0,0), (-1,0), C_H1),
        ("FONTNAME",     (0,0), (-1,0), "Helvetica-Bold"),
        ("FONTSIZE",     (0,0), (-1,-1), 9),
        ("ROWBACKGROUNDS",(0,1), (-1,-1), [C_TABLE_ROW, C_TABLE_ALT]),
        ("TEXTCOLOR",    (0,1), (-1,-1), C_TEXT),
        ("FONTNAME",     (0,1), (-1,-1), "Helvetica"),
        ("GRID",         (0,0), (-1,-1), 0.5, colors.HexColor("#2a2a4a")),
        ("ALIGN",        (0,0), (-1,-1), "LEFT"),
        ("LEFTPADDING",  (0,0), (-1,-1), 8),
        ("RIGHTPADDING", (0,0), (-1,-1), 8),
        ("TOPPADDING",   (0,0), (-1,-1), 5),
        ("BOTTOMPADDING",(0,0), (-1,-1), 5),
        ("ROWBACKGROUNDS",(0,0), (-1,0), [C_TABLE_HDR]),
    ])
    t = Table(stack_data, colWidths=[3.5*cm, 8*cm, 4*cm])
    t.setStyle(ts)
    el.append(t)

    el.append(Spacer(1, 2.5*cm))
    el.append(HRFlowable(width="100%", thickness=1, color=C_PURPLE, spaceAfter=10))
    el.append(Paragraph("Versão 2.0  |  Maio 2026  |  207 classes Java  |  26 migrations Flyway", styles["cover_meta"]))
    el.append(Paragraph("Spring Boot 3.4.3 / Java 21 / Clean Architecture + DDD", styles["cover_meta"]))
    return el

# ─── HELPER FLOWABLES ──────────────────────────────────────────────────────────
def why_box(lines, styles):
    """Blue bordered box for 'Por que usamos?'"""
    content = [Paragraph("Por que usamos?", styles["label_why"])]
    for ln in lines:
        content.append(Paragraph(_esc(ln), styles["why_body"]))
    ts = TableStyle([
        ("BACKGROUND", (0,0), (-1,-1), C_WHY_BG),
        ("LEFTPADDING", (0,0), (-1,-1), 10),
        ("RIGHTPADDING",(0,0), (-1,-1), 10),
        ("TOPPADDING",  (0,0), (-1,-1), 8),
        ("BOTTOMPADDING",(0,0),(-1,-1), 8),
        ("BOX",         (0,0), (-1,-1), 2, C_WHY_BORDER),
        ("LINEABOVE",   (0,0), (-1,0),  3, C_WHY_BORDER),
    ])
    t = Table([[content]], colWidths=[PAGE_W - 3.6*cm])
    t.setStyle(ts)
    return [t, Spacer(1, 4)]

def ex_box(lines, styles):
    """Green bordered box for 'Exemplo prático'"""
    content = [Paragraph("Exemplo pratico:", styles["label_ex"])]
    for ln in lines:
        content.append(Paragraph(_esc(ln), styles["mono_small"]))
    ts = TableStyle([
        ("BACKGROUND", (0,0), (-1,-1), C_EX_BG),
        ("LEFTPADDING", (0,0), (-1,-1), 10),
        ("RIGHTPADDING",(0,0), (-1,-1), 10),
        ("TOPPADDING",  (0,0), (-1,-1), 8),
        ("BOTTOMPADDING",(0,0),(-1,-1), 8),
        ("BOX",         (0,0), (-1,-1), 2, C_EX_BORDER),
        ("LINEABOVE",   (0,0), (-1,0),  3, C_EX_BORDER),
    ])
    t = Table([[content]], colWidths=[PAGE_W - 3.6*cm])
    t.setStyle(ts)
    return [t, Spacer(1, 4)]

def tip_box(text, styles):
    """Yellow bordered box for tips/notes"""
    content = [Paragraph("Dica:", styles["label_tip"]),
               Paragraph(_esc(text), styles["why_body"])]
    ts = TableStyle([
        ("BACKGROUND", (0,0), (-1,-1), C_TIP_BG),
        ("LEFTPADDING", (0,0), (-1,-1), 10),
        ("RIGHTPADDING",(0,0), (-1,-1), 10),
        ("TOPPADDING",  (0,0), (-1,-1), 6),
        ("BOTTOMPADDING",(0,0),(-1,-1), 6),
        ("BOX",         (0,0), (-1,-1), 1.5, C_TIP_BORDER),
        ("LINEABOVE",   (0,0), (-1,0),  3, C_TIP_BORDER),
    ])
    t = Table([[content]], colWidths=[PAGE_W - 3.6*cm])
    t.setStyle(ts)
    return [t, Spacer(1, 4)]

def code_block(lines, styles):
    """Dark code block"""
    content = []
    for ln in lines:
        content.append(Paragraph(_esc(ln).replace(" ", "&nbsp;"), styles["mono_small"]))
    ts = TableStyle([
        ("BACKGROUND", (0,0), (-1,-1), C_CODE_BG),
        ("LEFTPADDING", (0,0), (-1,-1), 10),
        ("RIGHTPADDING",(0,0), (-1,-1), 10),
        ("TOPPADDING",  (0,0), (-1,-1), 8),
        ("BOTTOMPADDING",(0,0),(-1,-1), 8),
        ("BOX",         (0,0), (-1,-1), 1, colors.HexColor("#374151")),
    ])
    t = Table([[content]], colWidths=[PAGE_W - 3.6*cm])
    t.setStyle(ts)
    return [t, Spacer(1, 4)]

def make_table(data, col_widths, styles_obj):
    ts = TableStyle([
        ("BACKGROUND",    (0,0), (-1,0), C_TABLE_HDR),
        ("TEXTCOLOR",     (0,0), (-1,0), C_H1),
        ("FONTNAME",      (0,0), (-1,0), "Helvetica-Bold"),
        ("FONTSIZE",      (0,0), (-1,-1), 9),
        ("ROWBACKGROUNDS",(0,1), (-1,-1), [C_TABLE_ROW, C_TABLE_ALT]),
        ("TEXTCOLOR",     (0,1), (-1,-1), C_TEXT),
        ("FONTNAME",      (0,1), (-1,-1), "Helvetica"),
        ("GRID",          (0,0), (-1,-1), 0.4, colors.HexColor("#2a2a4a")),
        ("ALIGN",         (0,0), (-1,-1), "LEFT"),
        ("LEFTPADDING",   (0,0), (-1,-1), 7),
        ("RIGHTPADDING",  (0,0), (-1,-1), 7),
        ("TOPPADDING",    (0,0), (-1,-1), 4),
        ("BOTTOMPADDING", (0,0), (-1,-1), 4),
        ("VALIGN",        (0,0), (-1,-1), "TOP"),
    ])
    rows = []
    for row in data:
        rows.append([Paragraph(_esc(cell), styles_obj["muted"] if i == 0 and data.index(row) > 0
                               else styles_obj["body"]) for i, cell in enumerate(row)])
    t = Table(rows, colWidths=col_widths, repeatRows=1)
    t.setStyle(ts)
    return t

def p(text, style):
    return Paragraph(_esc(text), style)

def sep(color=None):
    return HRFlowable(width="100%", thickness=0.5,
                      color=color or colors.HexColor("#2a2a4a"),
                      spaceAfter=6, spaceBefore=6)

def sp(h=0.2):
    return Spacer(1, h*cm)

# ─── CONTENT ───────────────────────────────────────────────────────────────────
def make_body(styles):
    B = styles["body"]
    BL = styles["bullet"]
    CO = styles["code"]
    MU = styles["muted"]

    el = []

    # ── SECTION 1: INTRODUCAO ──────────────────────────────────────────────────
    el.append(p("1. Introducao", styles["h1"]))
    el.append(sep(C_PURPLE))
    el.append(p(
        "O ServeFlow e um sistema de gestao para pequenos restaurantes, desenvolvido com foco em "
        "praticidade, seguranca e escalabilidade. Cobre o ciclo completo de operacao: usuarios, "
        "cardapios, pedidos, estoque, caixa e financeiro — com visualizacao em tempo real via WebSocket.",
        B))
    el.append(p(
        "Este documento serve como guia de estudo e referencia tecnica: para cada tecnologia e "
        "padrao adotado, explica o <b>motivo da escolha</b>, o <b>proposito dentro do sistema</b> "
        "e um <b>exemplo pratico concreto</b>.",
        B))
    el.append(sp())

    # ── SECTION 2: VISAO GERAL ─────────────────────────────────────────────────
    el.append(p("2. Visao Geral da Arquitetura", styles["h1"]))
    el.append(sep(C_PURPLE))
    el.append(p("O sistema e dividido em tres camadas principais hospedadas em plataformas distintas:", B))
    el.append(sp(0.3))

    infra_data = [
        ["Camada", "Plataforma", "Tecnologia Principal"],
        ["Frontend",  "Vercel",   "React 18, TailwindCSS, Axios, Zustand"],
        ["Backend",   "Render",   "Spring Boot 3.4.3, Java 21, Maven"],
        ["Banco",     "Supabase", "PostgreSQL 15, Connection Pooling, Flyway"],
        ["E-mail",    "Gmail",    "SMTP :587, STARTTLS"],
        ["CEP",       "ViaCEP",   "REST API publica — consulta de endereco"],
    ]
    el.append(make_table(infra_data, [3*cm, 3.5*cm, 9*cm], styles))
    el.append(sp())

    # ── SECTION 3: FRONTEND ────────────────────────────────────────────────────
    el.append(p("3. Frontend — Por que cada tecnologia?", styles["h1"]))
    el.append(sep(C_BLUE))

    # React 18
    el.append(p("3.1 React 18", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Biblioteca JavaScript para construcao de interfaces declarativas "
        "baseadas em componentes reutilizaveis.", B))
    el.extend(why_box([
        "React resolve o problema de manter a interface sincronizada com dados que mudam "
        "constantemente (status de pedidos, estoque, caixa) sem recarregar a pagina inteira. "
        "Re-renderiza apenas os componentes afetados — eficiente e responsivo."
    ], styles))
    el.append(p("<b>Para que serve no ServeFlow:</b> Cada tela (login, pedidos, KDS, caixa, "
                "estoque, financeiro) e um conjunto de componentes React reutilizaveis.", B))
    el.extend(ex_box([
        "O garcom seleciona um produto e a quantidade total do pedido atualiza instantaneamente.",
        "Sem React: seria necessario recarregar a pagina inteira ou manipulacao manual do DOM.",
        "Com React: apenas o componente 'TotalPedido' re-renderiza em milissegundos."
    ], styles))

    # React Router DOM
    el.append(p("3.2 React Router DOM", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Biblioteca de roteamento client-side — gerencia URLs sem "
        "recarregar o servidor (SPA navigation).", B))
    el.extend(why_box([
        "Em uma SPA, a navegacao entre telas nao pode usar reload de pagina — isso seria lento "
        "e perderia o estado da aplicacao. React Router intercepta cliques em links e troca "
        "apenas o componente exibido, mantendo a URL sincronizada."
    ], styles))
    el.extend(ex_box([
        "Gerente clica em 'Estoque' -> URL muda para /stock/consolidated",
        "Nenhuma requisicao e feita ao servidor para essa navegacao.",
        "O browser apenas troca o componente React exibido — rapido como trocar de aba.",
        "",
        "Rotas protegidas por perfil:",
        "  /orders     -> GARCOM, GERENTE, ADMIN",
        "  /kds        -> COZINHEIRO, GERENTE, ADMIN",
        "  /cashier    -> CAIXA, ADMIN",
        "  /financial  -> GERENTE, ADMIN"
    ], styles))

    # TailwindCSS
    el.append(p("3.3 TailwindCSS", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Framework CSS utility-first — estilizacao diretamente no JSX "
        "com classes padronizadas, sem arquivos CSS separados.", B))
    el.extend(why_box([
        "CSS tradicional exige criar arquivos .css para cada componente, gerando duplicacao "
        "e inconsistencias visuais. TailwindCSS centraliza um sistema de design tokens "
        "(cores, espacamentos, tipografia) aplicados como classes — garantindo consistencia."
    ], styles))
    el.extend(ex_box([
        "<button className=\"bg-green-500 hover:bg-green-600 text-white",
        "                   px-6 py-3 rounded-lg font-semibold",
        "                   transition-colors duration-200\">",
        "  Confirmar Pedido",
        "</button>",
        "",
        "Sem escrever um unico arquivo .css: cor, hover, padding, bordas e animacao.",
        "Classes 'md:grid-cols-2 lg:grid-cols-4' adaptam o layout para tablet e desktop."
    ], styles))

    # Axios
    el.append(p("3.4 Axios (com Interceptors)", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Cliente HTTP para JavaScript com suporte a promises e "
        "interceptors — codigo que executa automaticamente antes/depois de cada requisicao.", B))
    el.extend(why_box([
        "O fetch nativo do browser nao tem interceptors. Sem Axios, o tratamento de token "
        "expirado precisaria ser repetido em cada chamada HTTP do sistema. O interceptor "
        "centraliza essa logica: detecta 401, renova o token e reexecuta a requisicao — "
        "completamente transparente para o usuario."
    ], styles))
    el.extend(ex_box([
        "// Configurado uma unica vez no projeto:",
        "axios.interceptors.response.use(",
        "  response => response,",
        "  async error => {",
        "    if (error.response.status === 401) {",
        "      const newToken = await refreshToken(); // chama /auth/refresh",
        "      error.config.headers.Authorization = `Bearer ${newToken}`;",
        "      return axios(error.config); // reexecuta a requisicao",
        "    }",
        "    return Promise.reject(error);",
        "  }",
        ");",
        "",
        "Gerente consulta financeiro -> token de 15min expira -> Axios renova -> sem novo login."
    ], styles))

    # Zustand
    el.append(p("3.5 Zustand / Context API", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Zustand e uma biblioteca leve de estado global para React. "
        "Evita o 'prop drilling' (passar dados por multiplos componentes).", B))
    el.extend(why_box([
        "Em React, dados passados de pai para filho via props se tornam instaveis quando "
        "muitos niveis de componentes precisam do mesmo dado. Zustand resolve com uma store "
        "global simples e performatica — mais leve que Redux."
    ], styles))
    el.extend(ex_box([
        "// Qualquer componente acessa diretamente, sem prop drilling:",
        "const { user } = useAuthStore();",
        "",
        "// Menu lateral exibe/oculta itens por role:",
        "if (user.role === 'ADMIN') show(<ConfigMenu />)",
        "if (user.role === 'COZINHEIRO') show(<KdsMenu />)",
        "",
        "Sem Zustand: o 'user' precisaria descer por 5 niveis de componentes via props."
    ], styles))

    el.append(PageBreak())

    # ── SECTION 4: BACKEND ─────────────────────────────────────────────────────
    el.append(p("4. Backend — Por que cada tecnologia e padrao?", styles["h1"]))
    el.append(sep(C_PURPLE))

    # Spring Boot
    el.append(p("4.1 Spring Boot 3.4.3", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Framework Java que configura automaticamente todo o ecossistema Spring "
        "— servidor embutido (Tomcat), JPA, seguranca, WebSocket — com minimo de boilerplate "
        "(convention over configuration).", B))
    el.extend(why_box([
        "Spring Boot elimina o 'configuration hell' do Spring tradicional. Com apenas a anotacao "
        "@SpringBootApplication, o framework detecta as dependencias no classpath e configura "
        "tudo automaticamente — DataSource, EntityManager, filtros de seguranca, WebSocket broker."
    ], styles))
    el.extend(ex_box([
        "# application.yml — basta isso para conectar ao banco:",
        "spring:",
        "  datasource:",
        "    url: jdbc:postgresql://host:5432/serveflow_db",
        "    username: postgres",
        "    password: ${DB_PASSWORD}",
        "",
        "Sem Spring Boot: configuracao manual de DataSource, EntityManagerFactory,",
        "TransactionManager, HikariCP, Hibernate... centenas de linhas de XML.",
        "Com Spring Boot: zero configuracao adicional."
    ], styles))
    el.append(p("<b>Onde encontrar:</b> ServeflowApplication.java (classe principal).", MU))

    # Clean Architecture
    el.append(p("4.2 Clean Architecture", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Padrao arquitetural que separa regras de negocio (interno: Domain + Service) "
        "dos detalhes de implementacao (externo: HTTP, banco, frameworks).", B))
    el.extend(why_box([
        "Sem separacao de camadas, o codigo de negocio fica misturado com infraestrutura — "
        "impossivel testar sem banco de dados real, dificil de manter quando o framework muda. "
        "Clean Architecture garante que a logica de negocio nao depende de frameworks ou banco."
    ], styles))
    el.extend(ex_box([
        "Camada externa (Infraestrutura):",
        "  OrderController  -> recebe HTTP, valida DTO, chama OrderService",
        "  OrderEntity      -> mapeamento JPA (@Entity), conhece banco",
        "",
        "Camada interna (Dominio — sem dependencia de framework):",
        "  Order            -> regras de negocio puras",
        "  OrderService     -> orquestra casos de uso",
        "  OrderRepository  -> interface (nao implementacao!)",
        "",
        "Se migrarmos de PostgreSQL para MongoDB:",
        "  OrderEntity muda  -> OrderService, Order, OrderController NAO SAO TOCADOS."
    ], styles))

    # DDD
    el.append(p("4.3 DDD — Domain-Driven Design", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Abordagem onde o codigo reflete o vocabulario e as regras do dominio "
        "de negocio. Os objetos de dominio representam conceitos reais do restaurante.", B))
    el.extend(why_box([
        "Sem DDD, o codigo fica orientado a tabelas de banco (UserTable, OrderRow) em vez de "
        "conceitos de negocio (User, Order). DDD melhora a comunicacao entre devs e facilita "
        "entender regras de negocio diretamente no codigo-fonte."
    ], styles))
    el.extend(ex_box([
        "// Com DDD — a regra 'so confirma se CREATED' vive no dominio:",
        "public class Order {",
        "    public void confirm() {",
        "        if (this.status != OrderStatus.CREATED) {",
        "            throw new BusinessRuleException(\"Pedido ja processado\");",
        "        }",
        "        this.status = OrderStatus.CONFIRMED;",
        "    }",
        "}",
        "",
        "// Sem DDD — a regra estaria espalhada em 3 lugares diferentes:",
        "if (order.getStatus().equals(\"CREATED\")) { ... }  // no controller",
        "if (status == 0) { ... }                            // no service",
        "if (order.status.equals(\"CREATED\")) { ... }       // no repository"
    ], styles))

    # JWT
    el.append(p("4.4 JWT — JSON Web Token", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Padrao de token compacto e autocontido que representa claims (afirmacoes) "
        "sobre um usuario, assinado criptograficamente com HMAC-SHA256.", B))
    el.extend(why_box([
        "Autenticacao tradicional baseada em sessao exige que o servidor armazene e consulte "
        "sessoes a cada requisicao — nao escala horizontalmente. JWT e stateless: o token "
        "carrega os dados do usuario e e validado por criptografia, sem consultar banco de dados. "
        "Se escalarmos para 3 instancias do backend, todas validam o mesmo token."
    ], styles))
    el.extend(ex_box([
        "Estrutura do JWT (3 partes separadas por ponto):",
        "  Header:    eyJhbGciOiJIUzI1NiJ9",
        "             -> {\"alg\": \"HS256\"}",
        "",
        "  Payload:   eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiam9hbyIsInJvbGUiOiJHRVJFTlRFIn0",
        "             -> {\"userId\":1, \"username\":\"joao\", \"role\":\"GERENTE\", \"exp\":1748295600}",
        "",
        "  Assinatura: validada com JWT_SECRET (HMAC-SHA256)",
        "             -> garante que ninguem adulterou o payload",
        "",
        "Backend sabe que e Joao, gerente, sem consultar a tabela users."
    ], styles))
    el.append(p("<b>Onde encontrar:</b> config/JwtService.java", MU))

    # BCrypt
    el.append(p("4.5 BCrypt Password Encoder", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Algoritmo de hash adaptativo para senhas com salt automatico, "
        "projetado para ser intencionalmente lento e resistente a ataques de forca bruta.", B))
    el.extend(why_box([
        "MD5 e SHA-1 sao rapidos — um atacante pode testar bilhoes de senhas por segundo com GPU. "
        "BCrypt e intencionalmente lento (custo configuravel) e adiciona um salt unico por senha. "
        "Mesmo que o banco de dados seja comprometido, as senhas sao irrecuperaveis."
    ], styles))
    el.extend(ex_box([
        "Senha original:  \"admin123\"",
        "Hash BCrypt:     \"$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lih57\"",
        "",
        "Dois usuarios com a mesma senha 'admin123' terao hashes DIFERENTES (salt aleatorio).",
        "Impossivel reverter: nao existe funcao inversa para BCrypt.",
        "",
        "// Uso no UserService:",
        "String hash = passwordEncoder.encode(\"admin123\");    // ao cadastrar",
        "boolean ok  = passwordEncoder.matches(\"admin123\", hash); // ao logar"
    ], styles))
    el.append(p("<b>Onde encontrar:</b> config/SecurityConfig.java -> bean passwordEncoder()", MU))

    # JwtFilter
    el.append(p("4.6 JwtFilter — OncePerRequestFilter", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Filtro Spring Security que executa exatamente uma vez por requisicao HTTP, "
        "validando o token JWT antes de qualquer controller ser chamado.", B))
    el.extend(why_box([
        "Precisamos que toda requisicao autenticada seja validada antes de chegar ao controller. "
        "OncePerRequestFilter garante execucao unica mesmo em chains de filtros complexas. "
        "Sem este filtro, qualquer usuario poderia acessar qualquer endpoint sem token."
    ], styles))
    el.extend(ex_box([
        "GET /api/stock/items",
        "  Authorization: Bearer eyJ...",
        "",
        "JwtFilter executa (antes do StockController):",
        "  1. Extrai 'eyJ...' do header Authorization",
        "  2. JwtService.validateToken() -> OK (assinatura valida, nao expirado)",
        "  3. Extrai username='joao', role='GERENTE'",
        "  4. SecurityContextHolder.setAuthentication(...)",
        "",
        "Spring Security verifica RBAC:",
        "  /stock/** requer GERENTE ou ADMIN -> OK -> chega no StockController",
        "",
        "Se o token for invalido: retorna 401 Unauthorized imediatamente."
    ], styles))
    el.append(p("<b>Onde encontrar:</b> config/JwtFilter.java", MU))

    # CORS
    el.append(p("4.7 CORS — Cross-Origin Resource Sharing", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Mecanismo de seguranca dos browsers que bloqueia requisicoes HTTP "
        "entre dominios diferentes, a menos que o servidor explicitamente autorize.", B))
    el.extend(why_box([
        "Por padrao, o browser bloqueia chamadas de 'serveflow.vercel.app' para "
        "'serveflow.onrender.com' — sao dominios diferentes (cross-origin). "
        "Sem configurar CORS, o frontend nao consegue chamar a API em producao."
    ], styles))
    el.extend(ex_box([
        "SEM CORS configurado:",
        "  Erro no browser: 'Access to XMLHttpRequest at onrender.com",
        "  from origin vercel.app has been blocked by CORS policy'",
        "",
        "COM CORS (CORS_ALLOWED_ORIGINS=https://serveflow.vercel.app):",
        "  Servidor responde com header:",
        "  Access-Control-Allow-Origin: https://serveflow.vercel.app",
        "  -> Browser permite a chamada",
        "",
        "Em dev local: permite http://localhost:3000 e http://localhost:5173"
    ], styles))

    # @Async
    el.append(p("4.8 @Async — Execucao Assincrona", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Anotacao Spring que executa um metodo em uma thread separada do pool "
        "de threads, sem bloquear a thread principal que esta respondendo ao usuario.", B))
    el.extend(why_box([
        "Operacoes lentas (gravar logs no banco, enviar e-mail) nao devem atrasar a resposta "
        "ao usuario. Com @Async, essas operacoes rodam em paralelo — o usuario recebe o response "
        "imediatamente enquanto o trabalho pesado acontece em outra thread."
    ], styles))
    el.extend(ex_box([
        "SEM @Async — usuario espera negocio + log:",
        "  OrderService.complete()   ->  50ms (logica de negocio)",
        "  AuditService.logAction()  -> +200ms (INSERT no banco de log)",
        "  Response ao usuario       =  250ms  <- usuario espera tudo",
        "",
        "COM @Async — usuario espera apenas o negocio:",
        "  OrderService.complete()   ->  50ms  -> Response: 50ms (5x mais rapido)",
        "  AuditService.logAction()  ->  200ms (em outra thread, em paralelo)"
    ], styles))
    el.append(p("<b>Onde encontrar:</b> config/AsyncConfig.java + service/audit/AuditService.java", MU))

    el.append(PageBreak())

    # Spring Events
    el.append(p("4.9 Spring Events — @EventListener", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Mecanismo do Spring que permite publicar eventos e ter outros "
        "componentes reagindo sem se conhecerem diretamente — desacoplamento por eventos.", B))
    el.extend(why_box([
        "Sem eventos, OrderService teria que chamar diretamente CashierService.registerMovement() "
        "e KdsEventPublisher.notify() — acoplamento forte. Para adicionar notificacao por SMS "
        "seria necessario editar OrderService. Com eventos, OrderService apenas publica e "
        "qualquer componente pode escutar sem mudar o publicador."
    ], styles))
    el.extend(ex_box([
        "// OrderService apenas publica — nao sabe quem vai escutar:",
        "applicationEventPublisher.publishEvent(",
        "    new OrderCompletedEvent(order.getId(), order.getTotal())",
        ");",
        "",
        "// CashierEventListener reage de forma independente:",
        "@EventListener @Async",
        "public void onOrderCompleted(OrderCompletedEvent event) {",
        "    cashierService.registerMovement(event.getOrderId(), event.getTotal());",
        "}",
        "",
        "// Para adicionar SMS: cria novo listener, NAO TOCA em OrderService:",
        "@EventListener",
        "public void onOrderCompleted(OrderCompletedEvent event) {",
        "    smsService.notifyCustomer(event);",
        "}"
    ], styles))
    el.append(p("<b>Onde encontrar:</b> events/OrderCompletedEvent.java + service/cashier/CashierEventListener.java", MU))

    # @Scheduled
    el.append(p("4.10 @Scheduled — Tarefas Agendadas", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Anotacao Spring que agenda execucao automatica de metodos em "
        "intervalos definidos por expressao cron — sem intervencao manual.", B))
    el.extend(why_box([
        "Manutencao periodica (limpeza de logs antigos, verificacao de alertas de estoque) "
        "nao deve ser disparada manualmente. @Scheduled garante execucao automatica dentro "
        "do proprio aplicativo, sem precisar de cron job externo."
    ], styles))
    el.extend(ex_box([
        "@Scheduled(cron = \"0 0 2 * * ?\")  // Todo dia as 02:00",
        "public void cleanupOldLogs() {",
        "    // DELETE FROM access_log WHERE timestamp < NOW() - INTERVAL '90 days'",
        "    auditService.deleteLogsOlderThan(retentionDays);",
        "}",
        "",
        "Sem @Scheduled: tabela access_log cresceria indefinidamente.",
        "Com @Scheduled: limpeza automatica toda noite, zero intervencao humana."
    ], styles))

    # Repository Pattern
    el.append(p("4.11 Padrao Repository", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Padrao de design que abstrai o acesso a dados atras de uma interface. "
        "O service usa a interface, sem saber qual tecnologia de banco esta por baixo.", B))
    el.extend(why_box([
        "Sem o padrao Repository, o service importaria JpaRepository ou EntityManager "
        "diretamente — acoplamento entre logica de negocio e infraestrutura. Impossivel "
        "testar sem banco de dados real. Com Repository, podemos criar implementacoes "
        "in-memory para testes unitarios."
    ], styles))
    el.extend(ex_box([
        "// Interface de dominio — sem dependencia de framework:",
        "public interface UserRepository {",
        "    Optional<User> findByEmail(String email);",
        "    User save(User user);",
        "}",
        "",
        "// Implementacao com Spring Data JPA:",
        "public interface SpringUserRepository extends JpaRepository<UserEntity, Long> {",
        "    Optional<UserEntity> findByEmail(String email);",
        "}",
        "",
        "// Para testes — implementacao in-memory, zero banco:",
        "public class InMemoryUserRepository implements UserRepository {",
        "    private Map<String, User> store = new HashMap<>();",
        "    // UserService testado completamente sem PostgreSQL",
        "}"
    ], styles))

    # DTOs
    el.append(p("4.12 DTO — Data Transfer Objects", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Objetos simples usados para transferir dados entre camadas — "
        "separando o que entra (Input) do que sai (Output) e das entidades de banco.", B))
    el.extend(why_box([
        "Expor entidades JPA diretamente na API seria um problema: exporia campos internos "
        "(passwordHash, versao do Hibernate), dificultaria validacao e acoplaria a API ao banco. "
        "DTOs definem contratos claros e independentes da implementacao interna."
    ], styles))
    el.extend(ex_box([
        "// UserInput — o que o cliente ENVIA (com validacao):",
        "public record UserInput(",
        "    @NotBlank String name,",
        "    @Email @NotBlank String email,",
        "    @NotBlank @Size(min=8) String password,",
        "    @NotNull UserRole role",
        ") {}",
        "",
        "// UserOutput — o que o sistema RETORNA (sem campos sensiveis):",
        "public record UserOutput(",
        "    Long id, String name, String email, UserRole role, boolean active",
        ") {}",
        "",
        "// UserEntity — o que o banco ARMAZENA (NUNCA exposto na API):",
        "String passwordHash;  // nunca retornado",
        "Integer version;      // controle de concorrencia do Hibernate"
    ], styles))

    # WebSocket
    el.append(p("4.13 WebSocket + STOMP — Comunicacao em Tempo Real", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> WebSocket e um protocolo de comunicacao bidirecional e persistente. "
        "STOMP (Simple Text Oriented Messaging Protocol) adiciona conceitos de topicos, "
        "subscriptions e broker de mensagens sobre WebSocket.", B))
    el.extend(why_box([
        "HTTP e request/response — o cliente sempre inicia. Para o KDS receber novos pedidos "
        "instantaneamente e o caixa ver movimentos em tempo real, precisamos de server push: "
        "o servidor envia dados sem o cliente perguntar. Sem WebSocket seria necessario polling "
        "(requisicao a cada 2s) — ineficiente e com latencia alta."
    ], styles))
    el.extend(ex_box([
        "SEM WebSocket (polling a cada 2s):",
        "  Sistema faz GET /kds/orders a cada 2 segundos",
        "  -> 30 requisicoes/minuto por tablet -> banco sobrecarregado",
        "  -> Cozinheiro ve pedido com ate 2s de atraso",
        "",
        "COM WebSocket STOMP:",
        "  Garcom confirma pedido",
        "  -> OrderService publica OrderEvent",
        "  -> KdsEventPublisher envia para /topic/kds/orders",
        "  -> Todos os tablets conectados recebem em ~100ms automaticamente",
        "  -> Zero requisicoes de polling",
        "",
        "Topicos do ServeFlow:",
        "  /topic/kds/orders           -> atualizacao de status para cozinha",
        "  /topic/cashier/movements    -> novos movimentos de caixa",
        "  /topic/cashier/sessions     -> sessao aberta/fechada"
    ], styles))

    el.append(PageBreak())

    # ── SECTION 5: BANCO DE DADOS ──────────────────────────────────────────────
    el.append(p("5. Banco de Dados — Por que cada decisao?", styles["h1"]))
    el.append(sep(C_GREEN))

    # PostgreSQL
    el.append(p("5.1 PostgreSQL 15", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Sistema de gerenciamento de banco de dados relacional open source, "
        "robusto e com suporte a tipos avancados (UUID nativo, ENUM, JSONB, arrays).", B))
    el.extend(why_box([
        "O ServeFlow tem relacionamentos complexos: pedido -> itens -> adicionais -> produtos -> "
        "ingredientes -> receitas -> estoque. Um banco relacional com JOINs e a escolha natural. "
        "PostgreSQL e melhor suportado pelo Supabase e tem tipos nativos (UUID, ENUM) usados no sistema."
    ], styles))
    el.extend(ex_box([
        "-- Relatorio financeiro cruzando 4 tabelas:",
        "SELECT o.id, SUM(oi.total_price) as total, cs.opening_balance",
        "FROM orders o",
        "JOIN order_items oi ON oi.order_id = o.id",
        "JOIN cash_movements cm ON cm.order_id = o.id",
        "JOIN cash_sessions cs ON cs.id = cm.session_id",
        "WHERE o.status = 'COMPLETED'",
        "  AND DATE(o.created_at) = CURRENT_DATE",
        "GROUP BY o.id, cs.opening_balance;"
    ], styles))

    # Flyway
    el.append(p("5.2 Flyway — Versionamento do Banco", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Ferramenta de migracao de banco de dados que aplica scripts SQL "
        "versionados em ordem, uma unica vez, garantindo consistencia entre ambientes. "
        "E o 'Git do banco de dados'.", B))
    el.extend(why_box([
        "Sem Flyway, cada desenvolvedor teria um banco com schema diferente e o deploy em "
        "producao exigiria executar SQL manualmente — arriscado e dificil de rastrear. "
        "Flyway garante que dev, staging e producao sempre tenham exatamente a mesma estrutura."
    ], styles))
    el.extend(ex_box([
        "Desenvolvedor cria: V27__add_table_number_to_orders.sql",
        "  -> ALTER TABLE orders ADD COLUMN table_number INTEGER;",
        "",
        "Deploy em staging:",
        "  Flyway: 'V27 nao aplicada -> executando...'",
        "  -> ALTER TABLE aplicado automaticamente",
        "",
        "Deploy em producao (semana seguinte):",
        "  Flyway: 'V27 nao aplicada -> executando...'",
        "  -> Mesma alteracao, sem intervencao manual",
        "  -> Ambientes sempre sincronizados",
        "",
        "ServeFlow: 26 migrations (V1 users+auth -> V26 cash_movements)"
    ], styles))

    # UUID
    el.append(p("5.3 UUID como Primary Key", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Identificador Universal Unico — string de 36 caracteres gerada "
        "aleatoriamente, globalmente unica. Alternativa a IDs sequenciais (1, 2, 3...).", B))
    el.extend(why_box([
        "IDs sequenciais expoe informacoes: um usuario pode tentar GET /orders/1, /orders/2... "
        "e enumerar todos os pedidos do sistema (IDOR — Insecure Direct Object Reference). "
        "UUIDs sao imprevisíveis. Tambem permitem gerar IDs na aplicacao antes do INSERT, "
        "sem depender de sequencia do banco."
    ], styles))
    el.extend(ex_box([
        "ID sequencial: GET /api/orders/1, /api/orders/2, /api/orders/3",
        "  -> Atacante consegue listar todos os pedidos facilmente",
        "",
        "UUID: GET /api/orders/550e8400-e29b-41d4-a716-446655440000",
        "  -> Proximo UUID impossivel de prever",
        "  -> 2^122 possibilidades = 5.3 * 10^36 combinacoes",
        "",
        "// Geracao na aplicacao (antes do INSERT):",
        "order.setId(UUID.randomUUID());",
        "orderRepository.save(order);"
    ], styles))

    # Supabase
    el.append(p("5.4 Supabase — PostgreSQL Gerenciado", styles["h2"]))
    el.append(p(
        "<b>O que e:</b> Plataforma open source que provê PostgreSQL como servico com "
        "Connection Pooling, Auth, Storage, Real-time e dashboard integrados.", B))
    el.extend(why_box([
        "Configurar PostgreSQL do zero exigiria: provisionar VPS, instalar PostgreSQL, "
        "configurar SSL, backups, monitoramento, pgBouncer, fail-over... Supabase oferece "
        "tudo isso no free tier com uma linha de connection string."
    ], styles))

    supabase_data = [
        ["Recurso", "Por que usamos?", "Beneficio pratico"],
        ["Connection Pooling\n(pgBouncer)",
         "Cada conexao TCP ao PostgreSQL consome ~10MB RAM",
         "10 conexoes no pool servem 100 usuarios simultaneos"],
        ["SSL/TLS automatico",
         "Dados em transito criptografados",
         "Nenhuma configuracao manual de certificado"],
        ["Backups automaticos",
         "Recuperacao de desastres",
         "Restaurar banco em caso de falha critica"],
        ["PITR (Point-in-time\nRecovery)",
         "Backups sao snapshots; PITR restaura\npara qualquer segundo",
         "Dev apaga tabela -> restaura estado\nde 1 minuto atras"],
        ["Row Level Security",
         "Seguranca a nivel de linha no banco",
         "Mesmo com bug no app, banco protege dados"],
        ["Dashboard SQL Editor",
         "Queries ad-hoc sem cliente externo",
         "Gerente consulta dados pelo browser"],
    ]
    el.append(make_table(supabase_data, [3.5*cm, 5.5*cm, 6.5*cm], styles))
    el.append(sp(0.3))

    el.append(PageBreak())

    # ── SECTION 6: GUIA DO USUARIO ─────────────────────────────────────────────
    el.append(p("6. Guia do Usuario — Como Usar o Sistema", styles["h1"]))
    el.append(sep(C_GREEN))
    el.append(p(
        "Esta secao e destinada aos usuarios finais do ServeFlow. Aqui voce encontra, "
        "passo a passo, como realizar as operacoes do dia a dia de acordo com o seu perfil.", B))
    el.append(sp(0.3))

    # 6.1 Login
    el.append(p("6.1 Primeiro Acesso — Login", styles["h2"]))
    for item in [
        "1. Acesse o sistema pelo endereco fornecido pelo administrador",
        "2. Informe seu <b>e-mail</b> e <b>senha</b> -> clique em <b>Entrar</b>",
        "3. O sistema identifica seu perfil automaticamente e exibe apenas as funcionalidades do seu role",
    ]:
        el.append(p(item, BL))
    el.extend(tip_box(
        "Esqueceu a senha? Clique em 'Esqueci minha senha' -> informe o e-mail -> "
        "aguarde o link na caixa de entrada. O link expira em minutos — verifique spam se nao chegar.", styles))

    # 6.2 ADMIN
    el.append(p("6.2 Perfil ADMIN — Administrador", styles["h2"]))
    el.append(p("<b>Cadastrar usuario:</b>", styles["h4"]))
    for item in [
        "1. Menu <b>Usuarios</b> -> <b>Novo Usuario</b>",
        "2. Preencha: nome, e-mail, senha inicial e perfil (ADMIN, GERENTE, GARCOM, COZINHEIRO, CAIXA)",
        "3. Clique em <b>Salvar</b> -> comunique as credenciais ao colaborador",
    ]:
        el.append(p(item, BL))
    el.append(p("<b>Alterar cargo:</b> Usuarios -> colaborador -> <b>Alterar Cargo</b> -> selecionar -> confirmar", B))
    el.append(p("<b>Desativar usuario:</b> Usuarios -> colaborador -> <b>Desativar</b> -> perde acesso imediatamente", B))

    # 6.3 GERENTE
    el.append(p("6.3 Perfil GERENTE — Gestao do Restaurante", styles["h2"]))

    el.append(p("<b>Cadastrar produto:</b>", styles["h4"]))
    for item in [
        "1. <b>Produtos</b> -> <b>Novo Produto</b> -> nome, preco, tipo (PRODUTO ou INGREDIENTE)",
        "2. Upload de imagem opcional (PNG/JPG, max. 8MB)",
        "3. Marcar 'Requer ficha tecnica' se o produto consome ingredientes do estoque",
    ]:
        el.append(p(item, BL))

    el.append(p("<b>Criar cardapio:</b>", styles["h4"]))
    for item in [
        "1. <b>Cardapios</b> -> <b>Novo Cardapio</b> -> nome + turno (ALMOCO, JANTAR, etc.)",
        "2. Adicionar produtos -> preco personalizado por item (opcional)",
        "3. Configurar agendamento automatico (opcional — ativa/desativa no horario definido)",
    ]:
        el.append(p(item, BL))

    el.append(p("<b>Gerenciar estoque:</b>", styles["h4"]))
    ops = [
        ("<b>Entrada</b> (compra/reposicao)", "Estoque -> item -> Registrar Entrada -> quantidade + motivo"),
        ("<b>Perda</b> (vencimento/quebra)", "Estoque -> item -> Registrar Perda -> quantidade + motivo"),
        ("<b>Ajuste</b> (inventario fisico)", "Estoque -> item -> Ajuste -> nova quantidade real"),
        ("<b>Ficha Tecnica</b>", "Receitas -> Nova Receita -> produto -> ingredientes + quantidades"),
    ]
    for op, desc in ops:
        el.append(p(f"- {op}: {desc}", BL))

    el.append(p(
        "<b>Dica — Ficha Tecnica:</b> Apos cadastrar a receita, cada venda deste produto "
        "deduz automaticamente os ingredientes do estoque. Exemplo: pizza de frango debita "
        "200g de frango, 100g de queijo e 1 massa do estoque a cada pedido.", B))

    el.append(p("<b>Modulo Financeiro:</b>", styles["h4"]))
    for item in [
        "Conta a pagar/receber: Financeiro -> Contas -> Novo Lancamento -> descricao + valor + vencimento",
        "Liquidar: localizar conta -> Liquidar -> confirmar valor e data de pagamento",
        "Fluxo de caixa: Financeiro -> Fluxo de Caixa -> selecionar periodo",
    ]:
        el.append(p("- " + item, BL))

    # 6.4 GARCOM
    el.append(p("6.4 Perfil GARCOM — Registro de Pedidos", styles["h2"]))
    el.append(p("<b>Registrar novo pedido:</b>", styles["h4"]))
    for item in [
        "1. <b>Pedidos</b> -> <b>Novo Pedido</b> -> tipo: MESA, BALCAO ou DELIVERY",
        "2. MESA: informar numero da mesa  |  DELIVERY: informar CEP (endereco preenchido auto via ViaCEP)",
        "3. Selecionar itens + quantidades + adicionais (ex: 'sem cebola', 'extra queijo')",
        "4. Forma de pagamento -> <b>Confirmar Pedido</b> -> KDS recebe instantaneamente",
    ]:
        el.append(p(item, BL))

    status_data = [
        ["Status", "Significado", "Acao do garcom"],
        ["CRIADO",     "Pedido registrado",             "Aguardar confirmacao"],
        ["CONFIRMADO", "Na fila da cozinha",            "Aguardar preparo"],
        ["PREPARANDO", "Cozinha em preparo",            "Aguardar"],
        ["PRONTO",     "Pedido pronto para retirada",   "Buscar na cozinha -> clicar Entregar"],
        ["ENVIADO",    "Entregue ao cliente",           "Aguardar liquidacao no caixa"],
        ["CONCLUIDO",  "Finalizado e pago",             "—"],
    ]
    el.append(make_table(status_data, [3*cm, 5*cm, 7.5*cm], styles))
    el.append(sp(0.3))

    # 6.5 COZINHEIRO
    el.append(p("6.5 Perfil COZINHEIRO — KDS (Kitchen Display System)", styles["h2"]))
    el.append(p(
        "A tela do KDS se atualiza automaticamente via WebSocket — sem necessidade de "
        "recarregar a pagina. Pedidos aparecem em ~100ms apos o garcom confirmar.", B))
    for item in [
        "1. Pedido 'CONFIRMADO' aparece -> clicar <b>Iniciar Preparo</b> -> status: PREPARANDO",
        "2. Preparo concluido -> clicar <b>Pronto</b> -> garcom e notificado para retirar",
        "3. Item esgotado: Cardapio -> desativar disponibilidade -> garcom nao consegue mais pedir",
    ]:
        el.append(p(item, BL))
    el.extend(tip_box("Mantenha o KDS em uma tela dedicada sempre visivel na cozinha. "
                       "A atualizacao e automatica — nao precisa recarregar a pagina nunca.", styles))

    # 6.6 CAIXA
    el.append(p("6.6 Perfil CAIXA — Controle de Caixa", styles["h2"]))
    el.append(p("<b>Abrir caixa</b> (inicio do turno):", styles["h4"]))
    for item in [
        "1. <b>Caixa</b> -> <b>Abrir Caixa</b> -> informar saldo inicial -> confirmar",
        "2. So e possivel ter uma sessao aberta por vez — feche o turno anterior primeiro",
    ]:
        el.append(p(item, BL))

    el.append(p("<b>Liquidar pedido</b> (cliente pede a conta):", styles["h4"]))
    for item in [
        "1. <b>Caixa</b> -> <b>Pedidos Pendentes</b> -> localizar pelo numero da mesa ou ID",
        "2. Clicar <b>Liquidar</b> -> confirmar valor recebido e forma de pagamento",
        "3. Sistema registra pagamento e fecha o pedido automaticamente",
    ]:
        el.append(p(item, BL))

    el.append(p("<b>Movimentos manuais</b> (sangria, troco, retirada):", styles["h4"]))
    el.append(p("Caixa -> <b>Novo Movimento</b> -> Entrada ou Saida -> valor + descricao", B))
    el.append(p(
        "<b>Movimentos de pedidos concluidos sao lancados automaticamente</b> — "
        "o sistema cria o movimento de caixa via CashierEventListener quando um pedido e concluido.", B))

    el.append(p("<b>Fechar caixa</b> (fim do turno):", styles["h4"]))
    for item in [
        "1. <b>Caixa</b> -> <b>Fechar Caixa</b> -> revisar resumo do turno",
        "2. Informar saldo real contado em caixa",
        "3. Se houver diferenca, ela e registrada automaticamente",
        "4. Clicar <b>Confirmar Fechamento</b> — nenhum movimento pode ser adicionado apos isso",
    ]:
        el.append(p(item, BL))
    el.append(sp())

    # Resolucao de problemas
    el.append(p("6.7 Resolucao de Problemas", styles["h2"]))
    prob_data = [
        ["Situacao", "Solucao"],
        ["Item nao aparece no cardapio",
         "Produto deve estar ativo E item marcado como disponivel no cardapio"],
        ["Pedido travado em CONFIRMADO",
         "Cozinheiro deve clicar 'Iniciar Preparo' no KDS"],
        ["Estoque caindo sem vendas manuais",
         "Verificar ficha tecnica — pode haver desconto automatico por receita"],
        ["Nao consigo abrir o caixa",
         "Sessao anterior deve ser fechada primeiro (uma sessao por vez)"],
        ["E-mail de reset nao chegou",
         "Verificar spam; confirmar e-mail correto com administrador"],
        ["KDS nao atualiza",
         "Verificar conexao com internet — WebSocket requer conexao ativa"],
    ]
    el.append(make_table(prob_data, [6*cm, 9.5*cm], styles))

    el.append(PageBreak())

    # ── SECTION 7: INFRAESTRUTURA ──────────────────────────────────────────────
    el.append(p("7. Infraestrutura e Deploy", styles["h1"]))
    el.append(sep(C_YELLOW))

    el.append(p("7.1 Variaveis de Ambiente", styles["h2"]))
    env_data = [
        ["Variavel", "Descricao", "Padrao (dev)"],
        ["SPRING_PROFILES_ACTIVE", "Perfil ativo", "dev"],
        ["DATABASE_URL", "URL JDBC do PostgreSQL", "—"],
        ["JWT_SECRET", "Chave HMAC-SHA256 (min. 256 bits)", "obrigatorio"],
        ["JWT_EXPIRATION", "Expiracao do access token (ms)", "900000 (15min)"],
        ["CORS_ALLOWED_ORIGINS", "Origins permitidas pelo CORS", "http://localhost:3000"],
        ["MAIL_HOST", "Servidor SMTP", "smtp.gmail.com"],
        ["MAIL_PORT", "Porta SMTP", "587"],
        ["MAIL_USERNAME", "Usuario SMTP", "obrigatorio"],
        ["MAIL_PASSWORD", "Senha SMTP", "obrigatorio"],
        ["MAIL_FROM", "Remetente dos e-mails", "obrigatorio"],
        ["APP_BASE_URL", "URL base para imagens", "http://localhost:8080/api"],
        ["AUDIT_RETENTION_DAYS", "Retencao dos logs", "90"],
        ["SWAGGER_ENABLED", "Ativa Swagger UI (false em prod)", "true"],
    ]
    el.append(make_table(env_data, [5.5*cm, 6*cm, 4*cm], styles))
    el.append(sp(0.3))

    el.append(p("7.2 Checklist de Deploy em Producao", styles["h2"]))
    for item in [
        "[ ] Todas as variaveis de ambiente configuradas no Render",
        "[ ] SPRING_PROFILES_ACTIVE=prod",
        "[ ] SWAGGER_ENABLED=false  (nao expor API docs em producao)",
        "[ ] CORS_ALLOWED_ORIGINS aponta para o dominio Vercel correto",
        "[ ] Banco de dados Supabase acessivel pelo IP do Render",
        "[ ] Migrations Flyway executadas com sucesso (verificar logs de startup)",
        "[ ] Health check OK: GET /api/actuator/health -> 200",
        "[ ] Verificar logs de auditoria apos primeiro acesso",
    ]:
        el.append(p(item, BL))
    el.append(sp())

    # ── SECTION 8: BOAS PRATICAS ───────────────────────────────────────────────
    el.append(p("8. Boas Praticas e Manutencao", styles["h1"]))
    el.append(sep(C_YELLOW))

    el.append(p("8.1 Adicionando Novo Modulo", styles["h2"]))
    el.append(p("Para adicionar um novo modulo (ex.: reservas de mesa), siga a estrutura:", B))
    el.extend(code_block([
        "1.  V27__create_reservations.sql      -> migration Flyway (tabela no banco)",
        "2.  ReservationEntity.java            -> mapeamento JPA (@Entity, @Table)",
        "3.  SpringReservationRepository.java  -> Spring Data JPA (JpaRepository)",
        "4.  Reservation.java                  -> dominio puro (sem frameworks)",
        "5.  ReservationRepository.java        -> interface de dominio",
        "6.  ReservationInput/Output.java      -> DTOs com Bean Validation",
        "7.  ReservationService.java           -> logica de negocio (@Service)",
        "8.  ReservationController.java        -> endpoint REST (@RestController)",
        "9.  ReservationNotFoundException.java -> excecao de dominio",
        "10. GlobalExceptionHandler.java       -> adicionar @ExceptionHandler"
    ], styles))

    el.append(p("8.2 Seguranca", styles["h2"]))
    for item in [
        "Nunca commitar secrets no repositorio — use variaveis de ambiente",
        "JWT_SECRET: minimo 256 bits de entropia (gerar com: openssl rand -base64 32)",
        "SWAGGER_ENABLED=false em producao — nao expor documentacao da API",
        "CORS: listar origens explicitamente, nunca usar '*' em producao",
        "Refresh tokens sao rotacionados a cada uso — invalidacao automatica se comprometidos",
    ]:
        el.append(p("- " + item, BL))

    el.append(p("8.3 Auditoria e Monitoramento", styles["h2"]))
    audit_data = [
        ["Tabela", "Conteudo", "Retencao padrao"],
        ["access_log", "IP, endpoint, metodo, status, timestamp de toda requisicao", "90 dias"],
        ["audit_log", "Usuario, acao (LOGIN, CREATE, UPDATE, DELETE), entidade, detalhe", "90 dias"],
        ["error_log", "Endpoint, mensagem de erro, stack trace completo", "90 dias"],
    ]
    el.append(make_table(audit_data, [3.5*cm, 8.5*cm, 3.5*cm], styles))
    el.append(p(
        "O AuditLogCleanupJob (@Scheduled) remove automaticamente registros mais antigos que "
        "AUDIT_RETENTION_DAYS dias — evitando crescimento indefinido das tabelas de log.", B))
    el.append(sp())

    # ── REFERENCIA RAPIDA ──────────────────────────────────────────────────────
    el.append(p("Referencia Rapida — Endpoints Principais", styles["h1"]))
    el.append(sep(C_PURPLE))

    endpoints_data = [
        ["Endpoint", "Metodo", "Descricao", "Perfil"],
        ["/api/auth/login",              "POST",  "Login — retorna JWT + refreshToken",     "Publico"],
        ["/api/auth/refresh",            "POST",  "Renovar access token",                   "Publico"],
        ["/api/auth/forgot-password",    "POST",  "Solicitar reset de senha por e-mail",    "Publico"],
        ["/api/auth/reset-password",     "POST",  "Confirmar nova senha com token",         "Publico"],
        ["/api/products",                "GET",   "Listar produtos ativos",                 "Autenticado"],
        ["/api/products",                "POST",  "Cadastrar produto",                      "ADMIN/GERENTE"],
        ["/api/menus/active",            "GET",   "Cardapio ativo do turno atual",          "Autenticado"],
        ["/api/orders",                  "POST",  "Registrar pedido",                       "GARCOM+"],
        ["/api/orders/{id}/confirm",     "PATCH", "Confirmar pedido",                       "GARCOM+"],
        ["/api/orders/{id}/complete",    "PATCH", "Concluir pedido",                        "CAIXA+"],
        ["/api/kds/orders",              "GET",   "Fila de pedidos para cozinha",           "COZINHEIRO+"],
        ["/api/kds/orders/{id}/prepare", "PATCH", "Iniciar preparo",                        "COZINHEIRO+"],
        ["/api/kds/orders/{id}/ready",   "PATCH", "Marcar como pronto",                     "COZINHEIRO+"],
        ["/api/cashier/session/open",    "POST",  "Abrir sessao de caixa",                  "CAIXA+"],
        ["/api/cashier/session/{id}/close","POST","Fechar sessao de caixa",                 "CAIXA+"],
        ["/api/cashier/orders/{id}/settle","POST","Liquidar pedido no caixa",               "CAIXA+"],
        ["/api/stock/report/consolidated","GET",  "Relatorio consolidado de estoque",       "GERENTE+"],
        ["/api/dashboard/metrics",       "GET",   "Metricas gerais do restaurante",         "GERENTE+"],
        ["/api/financial/cash-flow",     "GET",   "Fluxo de caixa por periodo",             "GERENTE+"],
    ]
    el.append(make_table(endpoints_data, [5*cm, 1.8*cm, 6.5*cm, 2.2*cm], styles))
    el.append(sp(0.5))

    el.append(HRFlowable(width="100%", thickness=1, color=C_PURPLE, spaceAfter=8))
    el.append(p(
        "ServeFlow — 207 classes Java  |  26 migrations Flyway  |  11 controllers  |  "
        "14 services  |  19 repositorios  |  Clean Architecture + DDD",
        styles["muted"]))

    return el

# ─── RUN ───────────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    output = "C:/Users/Anderson Ramos/backend-serveflow/docs/ServeFlow-Manual-Arquitetura.pdf"
    build_doc(output)

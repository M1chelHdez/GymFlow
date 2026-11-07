package com.gymflow.sale;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SaleService {
    private final JdbcTemplate jdbc;
    public SaleService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public SaleResponse create(UUID gymId, CreateSaleRequest request) {
        if (jdbc.queryForObject("select count(*) from gyms where id=?", Integer.class, gymId) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gimnasio no encontrado.");
        String memberName = null;
        if (request.memberId() != null) {
            List<String> names = jdbc.query("select full_name from members where id=? and gym_id=? and status='ACTIVE'",
                (rs, row) -> rs.getString(1), request.memberId(), gymId);
            if (names.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El socio no existe o no pertenece a este gimnasio.");
            memberName = names.get(0);
        }

        record Line(UUID productId, String name, int quantity, BigDecimal price, BigDecimal cost) {}
        List<Line> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CreateSaleRequest.SaleLineRequest item : request.items()) {
            List<Line> found = jdbc.query("select id, name, sale_price, cost_price from products where id=? and gym_id=? and status='ACTIVE'",
                (rs, row) -> new Line(rs.getObject("id", UUID.class), rs.getString("name"), item.quantity(), rs.getBigDecimal("sale_price"), rs.getBigDecimal("cost_price")), item.productId(), gymId);
            if (found.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno de los productos no existe, está inactivo o pertenece a otro gimnasio.");
            Line line = found.get(0);
            // Combine duplicate product lines so each stock balance is checked and decremented once.
            int existing = -1;
            for (int i=0;i<lines.size();i++) if (lines.get(i).productId().equals(line.productId())) { existing=i; break; }
            if (existing >= 0) {
                Line prior = lines.get(existing);
                lines.set(existing, new Line(prior.productId(), prior.name(), prior.quantity()+line.quantity(), prior.price(), prior.cost()));
            } else lines.add(line);
        }
        for (Line line : lines) {
            int updated = jdbc.update("update stock_balances set quantity=quantity-?, version=version+1 where gym_id=? and product_id=? and quantity>=?",
                line.quantity(), gymId, line.productId(), line.quantity());
            if (updated != 1) throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para " + line.name() + ". Revisa las existencias antes de confirmar la venta.");
            subtotal = subtotal.add(line.price().multiply(BigDecimal.valueOf(line.quantity())));
        }
        Long folioNumber = jdbc.queryForObject("select nextval('sales_folio_seq')", Long.class);
        String folio = "VTA-%06d".formatted(folioNumber == null ? 1 : folioNumber);
        UUID saleId = jdbc.queryForObject("insert into sales(gym_id,folio,member_id,payment_method,subtotal,discount_total,total) values(?,?,?,?,?,0,?) returning id",
            UUID.class, gymId, folio, request.memberId(), request.paymentMethod(), subtotal, subtotal);
        for (Line line : lines) {
            BigDecimal lineTotal = line.price().multiply(BigDecimal.valueOf(line.quantity()));
            jdbc.update("insert into sale_items(sale_id,product_id,quantity,unit_price,unit_cost,line_total) values(?,?,?,?,?,?)",
                saleId, line.productId(), line.quantity(), line.price(), line.cost(), lineTotal);
            jdbc.update("insert into stock_movements(gym_id,product_id,type,quantity,reason) values(?,?,'OUT',?,?)",
                gymId, line.productId(), line.quantity(), "Venta de producto · " + folio);
        }
        return getOne(gymId, saleId);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> list(UUID gymId) {
        return jdbc.query("select s.id,s.folio,s.gym_id,s.member_id,m.full_name,s.status,s.payment_method,s.payment_status,s.subtotal,s.discount_total,s.total,(select coalesce(sum(si.quantity),0) from sale_items si where si.sale_id=s.id) item_count,s.created_at from sales s left join members m on m.id=s.member_id where s.gym_id=? order by s.created_at desc",
            (rs, row) -> new SaleResponse(rs.getObject("id",UUID.class), rs.getString("folio"), rs.getObject("gym_id",UUID.class), rs.getObject("member_id",UUID.class), rs.getString("full_name"), rs.getString("status"), rs.getString("payment_method"), rs.getString("payment_status"), rs.getBigDecimal("subtotal"), rs.getBigDecimal("discount_total"), rs.getBigDecimal("total"), rs.getInt("item_count"), rs.getTimestamp("created_at").toInstant()), gymId);
    }

    private SaleResponse getOne(UUID gymId, UUID saleId) {
        return jdbc.queryForObject("select s.id,s.folio,s.gym_id,s.member_id,m.full_name,s.status,s.payment_method,s.payment_status,s.subtotal,s.discount_total,s.total,(select coalesce(sum(si.quantity),0) from sale_items si where si.sale_id=s.id) item_count,s.created_at from sales s left join members m on m.id=s.member_id where s.gym_id=? and s.id=?",
            (rs, row) -> new SaleResponse(rs.getObject("id",UUID.class), rs.getString("folio"), rs.getObject("gym_id",UUID.class), rs.getObject("member_id",UUID.class), rs.getString("full_name"), rs.getString("status"), rs.getString("payment_method"), rs.getString("payment_status"), rs.getBigDecimal("subtotal"), rs.getBigDecimal("discount_total"), rs.getBigDecimal("total"), rs.getInt("item_count"), rs.getTimestamp("created_at").toInstant()), gymId, saleId);
    }
}

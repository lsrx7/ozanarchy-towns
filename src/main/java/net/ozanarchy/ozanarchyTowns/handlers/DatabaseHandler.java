package net.ozanarchy.ozanarchyTowns.handlers;

import net.ozanarchy.ozanarchyTowns.OzanarchyTowns;
import org.bukkit.Chunk;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseHandler {
    private final OzanarchyTowns plugin;

    public DatabaseHandler(OzanarchyTowns plugin) {
        this.plugin = plugin;
    }

    public boolean getChunkClaimed(Chunk chunk){
        String sql = """
            SELECT 1 FROM claims
            WHERE world=? AND chunkx=? AND chunkz=?
            LIMIT 1
        """;

        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setString(1, chunk.getWorld().getName());
            stmt.setInt(2, chunk.getX());
            stmt.setInt(3, chunk.getZ());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void saveClaim(Chunk chunk, int townID){
        String sql = """
            INSERT INTO claims (world, chunkx, chunkz, town_id)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setString(1, chunk.getWorld().getName());
            stmt.setInt(2, chunk.getX());
            stmt.setInt(3, chunk.getZ());
            stmt.setInt(4, townID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Integer getPlayerTownId(UUID uuid) {
        String sql = """
            SELECT town_id
            FROM town_members
            WHERE uuid = ?
            LIMIT 1
        """;

        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("town_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getTownName(int townId) {
        String sql = """
            SELECT name
            FROM towns
            WHERE id = ?
            LIMIT 1
        """;

        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, townId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Integer getChunkTownId(Chunk chunk) {
        String sql = """
            SELECT town_id FROM claims
            WHERE world=? AND chunkx=? AND chunkz=?
            LIMIT 1
        """;

        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setString(1, chunk.getWorld().getName());
            stmt.setInt(2, chunk.getX());
            stmt.setInt(3, chunk.getZ());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("town_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean isMember(UUID uuid, int townId) {
        String sql = """
            SELECT 1 FROM town_members
            WHERE uuid = ? AND town_id = ?
            LIMIT 1
        """;

        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, townId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean townExists(String name){
        String sql = "SELECT 1 FROM towns WHERE name = ? LIMIT 1";

        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)){
            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()){
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int createTown(String name, UUID mayor) throws SQLException {
        String sql = "INSERT INTO towns (name, mayor_uuid) VALUES (?, ?)";

        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, mayor.toString());
            stmt.executeUpdate();

            try(ResultSet rs = stmt.getGeneratedKeys()) {
                if(rs.next()){
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create town");
    }

    public boolean addMember(int townId, UUID uuid, String role){
        String sql = """
                INSERT INTO town_members (town_id, uuid, role)
                VALUES (?, ?, ?)
                """;

        try(PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, townId);
            stmt.setString(2, uuid.toString());
            stmt.setString(3, role);
            return stmt.executeUpdate() > 0;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
    public boolean setRole(UUID uuid, int townId, String role){
        String sql = """
            UPDATE town_members
            SET role=?
            WHERE uuid=? AND town_id=?
        """;

        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)){
            stmt.setString(1, role);
            stmt.setString(2, uuid.toString());
            stmt.setInt(3, townId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean isMemberRank(UUID uuid, int townId){
        String sql = """
            SELECT 1 FROM town_members
            WHERE uuid=? AND town_id=? AND role='MEMBER'
            LIMIT 1
        """;

        try(PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)){
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, townId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeMember(UUID uuid, int townId){
        String sql = "DELETE FROM town_members WHERE uuid=? AND town_id=?";

        try(PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)){
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, townId);
            return stmt.executeUpdate() > 0;
        } catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }

    public boolean isMayor(UUID uuid, int townId) {
        String sql = """
            SELECT 1 FROM town_members
            WHERE uuid=? AND town_id=? AND role='MAYOR'
            LIMIT 1
        """;

        try(PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, townId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void deleteClaim(int townId){
        String sql = "DELETE FROM claims WHERE town_id=?";

        try(PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)){
            stmt.setInt(1, townId);
            stmt.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void deleteTown(int townId){
        String sql = "DELETE FROM towns WHERE id=?";

        try(PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, townId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMembers(int townId){
        String sql = "DELETE FROM town_members WHERE town_id=?";

        try(PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)){
            stmt.setInt(1, townId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean unClaimChunk(Chunk chunk, int townId){
        String sql = """
                DELETE FROM claims
                WHERE world=? AND chunkx=? AND chunkz=? AND town_id=?
                LIMIT 1
                """;
        try(PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setString(1, chunk.getWorld().getName());
            stmt.setInt(2, chunk.getX());
            stmt.setInt(3, chunk.getZ());
            stmt.setInt(4, townId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isTownAdmin(UUID uuid, int townId){
        String sql = """
                SELECT 1 FROM town_members
                WHERE uuid=? AND town_id=? AND role IN ('MAYOR','OFFICER')
                LIMIT 1
                """;
        try(PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)){
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, townId);

            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean canBuild(UUID uuid, Chunk chunk){
        String sql = """
            SELECT 1
            FROM claims c
            JOIN town_members m ON c.town_id = m.town_id
            WHERE c.world=? AND c.chunkx=? AND c.chunkz=?
            AND m.uuid=?
            LIMIT 1
        """;
        try(PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setString(1, chunk.getWorld().getName());
            stmt.setInt(2, chunk.getX());
            stmt.setInt(3, chunk.getZ());
            stmt.setString(4, uuid.toString());

            return stmt.executeQuery().next();
        } catch (SQLException e){
            e.printStackTrace();
        }

        return false;
    }

    //BANKING AND UPKEEP
    public void createTownBank(int townId){
        String sql = "INSERT INTO town_bank (town_id) VALUES (?)";

        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, townId);
            stmt.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void deleteTownBank(int townId){
        String sql = "DELETE FROM town_bank WHERE town_id=?";

        try(PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)){
            stmt.setInt(1, townId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean depositTownMoney(int townId, double amount) {
        String sql = "UPDATE town_bank SET balance = balance + ? WHERE town_id=?";
        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, townId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean withdrawTownMoney(int townId, double amount){
        String sql = """
                UPDATE town_bank
                SET balance = balance - ?
                WHERE town_id=? AND balance >=?
                """;
        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)){
            stmt.setDouble(1, amount);
            stmt.setInt(2, townId);
            stmt.setDouble(3, amount);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getTownBalance(int townId){
        String sql = "SELECT balance FROM town_bank WHERE town_id=? LIMIT 1";
        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)){
            stmt.setInt(1, townId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void increaseUpkeep(int townId, double amount){
        String sql = "UPDATE town_bank SET upkeep_cost = upkeep_cost + ? WHERE town_id=?";
        try(PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, townId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void decreaseUpkeep(int townId, double amount){
        String sql = "UPDATE town_bank SET upkeep_cost = upkeep_cost - ? WHERE town_id=?";
        try(PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, townId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLastUpkeep(int townId) {
        String sql = "UPDATE town_bank SET last_upkeep = NOW() WHERE id = ?";
        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, townId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet getTownsNeedingUpkeep() throws SQLException {
        String sql = """
        SELECT tb.town_id, tb.balance, tb.upkeep_cost, tb.last_upkeep
        FROM town_bank tb
        WHERE UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(tb.last_upkeep) >= tb.upkeep_interval
    """;
        return plugin.getConnection().prepareStatement(sql).executeQuery();
    }

    public Map<String, Integer> loadAllClaims(){
        Map<String, Integer> claims = new ConcurrentHashMap<>();
        String sql = "SELECT world, chunkx, chunkz, town_id FROM claims";
        try (PreparedStatement stmt = plugin.getConnection().prepareStatement(sql)){
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                String world = rs.getString("world");
                int x = rs.getInt("chunkx");
                int z = rs.getInt("chunkz");
                int townId = rs.getInt("town_id");

                claims.put(world + ":" + x + ":" + z, townId);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return claims;
    }
}

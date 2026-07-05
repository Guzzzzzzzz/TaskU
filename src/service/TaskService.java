package service;

import database.MataKuliahDAO;
import database.TaskDAO;
import model.AnggotaKelompok;
import model.Mahasiswa;
import model.MataKuliah;
import model.Task;
import model.Task.Jenis;
import model.Task.Prioritas;
import model.Task.Status;
import util.TranslationManager;

import java.time.LocalDate;
import java.util.List;

/**
 * Business Logic Layer — menjembatani GUI ↔ DAO.
 * Semua operasi di-context-kan ke mahasiswa yang sedang login.
 */
public class TaskService {

    private final TaskDAO taskDAO;
    private final MataKuliahDAO mataKuliahDAO;
    private final database.SettingsDAO settingsDAO;
    private final Mahasiswa loggedInUser;

    public TaskService(Mahasiswa loggedInUser) {
        this.taskDAO = new TaskDAO();
        this.mataKuliahDAO = new MataKuliahDAO();
        this.settingsDAO = new database.SettingsDAO();
        this.loggedInUser = loggedInUser;
    }

    public boolean isDarkMode() {
        return settingsDAO.isDarkMode(userId());
    }

    public String getLanguage() {
        return settingsDAO.getLanguage(userId());
    }

    public void saveSettings(boolean isDarkMode, String language) {
        settingsDAO.saveSettings(userId(), isDarkMode, language);
    }

    private int userId() {
        return loggedInUser.getId();
    }



    public List<Task> getAllTasks() {
        return taskDAO.getAllTasks(userId());
    }

    public List<Task> getTasksByStatus(Status status) {
        return taskDAO.getTasksByStatus(userId(), status);
    }

    public List<Task> getTasksByPriority(Prioritas prioritas) {
        return taskDAO.getTasksByPriority(userId(), prioritas);
    }

    public List<Task> getTasksByMataKuliah(int mkId) {
        return taskDAO.getTasksByMataKuliah(userId(), mkId);
    }

    public List<Task> searchTasks(String keyword) {
        return taskDAO.searchTasks(userId(), keyword);
    }

    public List<Task> getTasksToday() {
        return taskDAO.getTasksToday(userId());
    }

    public int getTotalTasks() {
        return taskDAO.getTotalTasks(userId());
    }

    public int getCompletedCount() {
        return taskDAO.getCompletedCount(userId());
    }

    public int getUrgentCount() {
        return taskDAO.getUrgentCount(userId());
    }

    public int getCategoryCount() {
        return mataKuliahDAO.getAllByMahasiswa(userId()).size();
    }

    public int getCompletionPercentage() {
        int total = getTotalTasks();
        if (total == 0) return 0;
        return (getCompletedCount() * 100) / total;
    }

    /** @return ID tugas baru jika berhasil, -1 jika gagal */
    public int addNewTask(String judul, String deskripsi, MataKuliah matkul, Prioritas prioritas,
                          LocalDate deadline, Jenis jenis, List<AnggotaKelompok> anggotaList) {

        if (judul == null || judul.trim().isEmpty()) {
            System.err.println("[TaskService] Judul tidak boleh kosong!");
            return -1;
        }

        Task task = new Task(
            judul.trim(), deskripsi != null ? deskripsi.trim() : "", deadline,
            prioritas, Status.BELUM, jenis,
            matkul, loggedInUser
        );


        if (jenis == Jenis.KELOMPOK && anggotaList != null) {
            task.setAnggotaList(anggotaList);
        }

        return taskDAO.addTask(task);
    }


    public void toggleTaskComplete(Task task) {
        Status newStatus = task.isCompleted() ? Status.BELUM : Status.SELESAI;
        taskDAO.updateStatus(task.getId(), newStatus);
        task.setStatus(newStatus);
    }


    public void updateStatus(Task task, Status newStatus) {
        taskDAO.updateStatus(task.getId(), newStatus);
        task.setStatus(newStatus);
    }


    public boolean updateDeskripsi(Task task, String deskripsi) {
        if (taskDAO.updateDeskripsi(task.getId(), deskripsi)) {
            task.setDeskripsi(deskripsi);
            return true;
        }
        return false;
    }


    public boolean deleteTask(int taskId) {
        return taskDAO.deleteTask(taskId);
    }



    public List<MataKuliah> getAllMataKuliah() {
        return mataKuliahDAO.getAllByMahasiswa(userId());
    }

    public int addMataKuliah(String nama, String warna) {
        return mataKuliahDAO.insert(nama, warna, userId());
    }

    public boolean deleteMataKuliah(int id) {
        return mataKuliahDAO.delete(id);
    }




    public String getDeadlineWarningMessage() {
        int urgent = getUrgentCount();
        if (urgent == 0) return null;
        String template = TranslationManager.currentLanguage == TranslationManager.Language.ID 
            ? "Ada %d tugas yang deadline-nya dalam 2 hari ke depan!"
            : "There are %d tasks due in the next 2 days!";
        return String.format(template, urgent);
    }


    public Mahasiswa getLoggedInUser() {
        return loggedInUser;
    }
}

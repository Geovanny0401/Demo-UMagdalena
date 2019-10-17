package com.gmail.geovanny.spring;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

import java.util.Collection;

@Route
@PWA(name = "Project Base for Vaadin Flow with Spring", shortName = "Project Base")
@Theme(value = Lumo.class, variant = Lumo.LIGHT)
public class MainView extends Composite<VerticalLayout> {

    private Button refresh = new Button("", VaadinIcon.REFRESH.create());
    private Button add = new Button("", VaadinIcon.PLUS.create());
    private Button edit = new Button("", VaadinIcon.PENCIL.create());

    private final IStudentRepo repository;
    private Grid<Student> grid = new Grid<>(Student.class);

    public MainView(IStudentRepo repository) {

        this.repository = repository;
        initLayout();
        initBehavior();
        refresh();
    }

    private void initLayout() {
        HorizontalLayout header = new HorizontalLayout(refresh, add, edit);
        grid.setColumns("idStudent", "firstName", "lastName");
        grid.addComponentColumn(user -> new Button("Delete", e -> deleteClicked(user)));
        grid.setSizeFull();
        getContent().add(header, grid);
        getContent().expand(grid);
        getContent().setSizeFull();
        getContent().setMargin(false);
        getContent().setPadding(false);
    }

    private void initBehavior() {
        grid.asSingleSelect().addValueChangeListener(e -> updateHeader());
        refresh.addClickListener(e -> refresh());
        add.addClickListener(e -> showAddDialog());
        edit.addClickListener(e -> showEditDialog());
    }

    public void refresh() {
        grid.setItems((Collection<Student>) repository.findAll());
        updateHeader();
    }

    private void updateHeader() {
        boolean selected = !grid.asSingleSelect().isEmpty();
        edit.setEnabled(selected);
    }

    private void deleteClicked(Student student) {
        showRemoveDialog(student);
        refresh();
    }

    private void showAddDialog() {
        UserFormDialog dialog = new UserFormDialog("New Student", new Student());
        dialog.open();
    }

    private void showEditDialog() {
        UserFormDialog dialog = new UserFormDialog("Update Student", grid.asSingleSelect().getValue());
        dialog.open();
    }


    private void showRemoveDialog(Student student) {
        RemoveDialog dialog = new RemoveDialog(student);
        dialog.open();
    }

    private class UserFormDialog extends Dialog {
        private TextField firstName = new TextField("First name");
        private TextField lastName = new TextField("Last name");
        private Button cancel = new Button("Cancel");
        private Button save = new Button("Save", VaadinIcon.CHECK.create());

        public UserFormDialog(String caption, Student student) {
            initLayout(caption);
            initBehavior(student);
        }

        private void initLayout(String caption) {
            save.getElement().setAttribute("theme", "primary");
            HorizontalLayout buttons = new HorizontalLayout(cancel, save);
            buttons.setSpacing(true);
            firstName.setRequiredIndicatorVisible(true);
            FormLayout formLayout = new FormLayout(new H2(caption), firstName, lastName);
            VerticalLayout layout = new VerticalLayout(formLayout, buttons);
            layout.setAlignSelf(FlexComponent.Alignment.END, buttons);
            add(layout);
        }

        private void initBehavior(Student student) {
            BeanValidationBinder<Student> binder = new BeanValidationBinder<>(Student.class);
            binder.bindInstanceFields(this);
            binder.readBean(student);
            cancel.addClickListener(e -> close());
            save.addClickListener(e -> {
                try {
                    binder.validate();
                    binder.writeBean(student);
                    repository.save(student);
                    close();
                    refresh();
                    Notification.show("Student saved");
                } catch (ValidationException ex) {
                    Notification.show("Please fix the errors and try again");
                }
            });
        }
    }

    //Implementacion Operacion Eliminar
    private class RemoveDialog extends Dialog {
        private Button cancel = new Button("Cancel");
        private Button delete = new Button("Delete", VaadinIcon.TRASH.create());

        public RemoveDialog(Student student) {
            initLayout(student);
            initBehavior(student);
        }

        private void initLayout(Student student) {



            Span span = new Span("Do you really want to delete the user " + student.getFirstName() + " " + student.getLastName() + "?");
            delete.getElement().setAttribute("theme", "error");
            HorizontalLayout buttons = new HorizontalLayout(cancel, delete);
            VerticalLayout layout = new VerticalLayout(new H2("Confirm"), span, buttons);
            layout.setAlignSelf(FlexComponent.Alignment.END, buttons);
            add(layout);


        }

        private void initBehavior(Student customer) {

            cancel.addClickListener(e -> close());
            delete.addClickListener(e -> {
                repository.deleteById(customer.getIdStudent());
                refresh();
                close();
            });
        }
    }
}

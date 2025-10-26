#include <iostream>
#include <iomanip>
#include <string>
#include <fstream>
#include <limits>
#include <algorithm>
using namespace std;

// ANSI color codes for terminal output
#define RESET   "\033[0m"
#define RED     "\033[31m"
#define GREEN   "\033[32m"
#define YELLOW  "\033[33m"
#define CYAN    "\033[36m"
#define MAGENTA "\033[35m"
#define BLUE    "\033[34m"
#define WHITE   "\033[37m"
#define BOLD    "\033[1m"

struct Vehicle {
    int id;
    string plateNumber;
    string type; // Car, Bike, Truck
    float charge;
    float duration; // Float to support minutes
    Vehicle* next;
};

Vehicle* head = nullptr;
bool isSortedByID = false; // Tracks if list is sorted by ID for binary search

string toLower(const string& str) {
    string result = str;
    transform(result.begin(), result.end(), result.begin(), ::tolower);
    return result;
}

bool isUniqueID(int id) {
    if (id <= 0) return false;
    Vehicle* temp = head;
    while (temp) {
        if (temp->id == id) return false;
        temp = temp->next;
    }
    return true;
}

bool isPlateDuplicate(string plate) {
    Vehicle* temp = head;
    while (temp) {
        if (temp->plateNumber == plate) return true;
        temp = temp->next;
    }
    return false;
}

void clearInputBuffer() {
    cin.clear();
    cin.ignore(numeric_limits<streamsize>::max(), '\n');
}

void addVehicle() {
    Vehicle* newVehicle = new Vehicle();
    cout << CYAN << "Enter ticket ID (positive integer): " << RESET;
    while (!(cin >> newVehicle->id) || newVehicle->id <= 0) {
        cout << RED << "Invalid ID. Enter a positive integer: " << RESET;
        clearInputBuffer();
    }
    if (!isUniqueID(newVehicle->id)) {
        cout << RED << "Duplicate ID. Must be unique!\n" << RESET;
        delete newVehicle;
        clearInputBuffer();
        return;
    }

    cout << CYAN << "Enter plate number: " << RESET;
    cin >> newVehicle->plateNumber;
    if (isPlateDuplicate(newVehicle->plateNumber)) {
        cout << YELLOW << "Warning: Plate number already exists.\n" << RESET;
    }

    cout << CYAN << "Enter vehicle type (Car/Bike/Truck): " << RESET;
    cin >> newVehicle->type;
    cout << CYAN << "Enter parking duration (in hours, e.g., 1.5 for 90 minutes): " << RESET;
    while (!(cin >> newVehicle->duration) || newVehicle->duration <= 0) {
        cout << RED << "Invalid duration. Enter a positive number: " << RESET;
        clearInputBuffer();
    }

    // Calculate charge (per hour)
    string typeLower = toLower(newVehicle->type);
    if (typeLower == "car") newVehicle->charge = 2.0f * newVehicle->duration;
    else if (typeLower == "bike") newVehicle->charge = 1.0f * newVehicle->duration;
    else if (typeLower == "truck") newVehicle->charge = 3.0f * newVehicle->duration;
    else {
        cout << YELLOW << "Unknown vehicle type. Default charge applied as Car rate.\n" << RESET;
        newVehicle->charge = 2.0f * newVehicle->duration;
    }

    newVehicle->next = head;
    head = newVehicle;
    isSortedByID = false; // Adding a vehicle may unsort the list
    cout << GREEN << "Vehicle added successfully! Charge: $" << fixed << setprecision(2) << newVehicle->charge << RESET << endl;
    cout << CYAN << "Press Enter to continue..." << RESET;
    clearInputBuffer();
}

void displayVehicles() {
    Vehicle* temp = head;
    cout << MAGENTA << "\n===== Vehicle List =====" << RESET << endl;
    if (!temp) {
        cout << YELLOW << "No vehicles to display." << RESET << endl;
        cout << CYAN << "Press Enter to continue..." << RESET;
        clearInputBuffer();
        return;
    }
    int index = 1;
    while (temp) {
        cout << BOLD << "Vehicle " << index++ << ":" << RESET << endl;
        cout << BLUE << "ID: " << RESET << temp->id << endl;
        cout << BLUE << "Plate Number: " << RESET << temp->plateNumber << endl;
        cout << BLUE << "Type: " << RESET << temp->type << endl;
        cout << BLUE << "Duration: " << RESET << fixed << setprecision(2) << temp->duration << " hrs" << endl;
        cout << BLUE << "Charge: " << RESET << "$" << fixed << setprecision(2) << temp->charge << endl;
        cout << MAGENTA << "---------------------" << RESET << endl;
        temp = temp->next;
    }
    cout << CYAN << "Press Enter to continue..." << RESET;
    clearInputBuffer();
}

void linearSearch() {
    int id;
    cout << CYAN << "Enter ID to search: " << RESET;
    while (!(cin >> id) || id <= 0) {
        cout << RED << "Invalid ID. Enter a positive integer: " << RESET;
        clearInputBuffer();
    }
    Vehicle* temp = head;
    while (temp) {
        if (temp->id == id) {
            cout << GREEN << "Vehicle Found!" << RESET << endl;
            cout << BLUE << "ID: " << RESET << temp->id << endl;
            cout << BLUE << "Plate Number: " << RESET << temp->plateNumber << endl;
            cout << BLUE << "Type: " << RESET << temp->type << endl;
            cout << BLUE << "Duration: " << RESET << fixed << setprecision(2) << temp->duration << " hrs" << endl;
            cout << BLUE << "Charge: " << RESET << "$" << fixed << setprecision(2) << temp->charge << endl;
            cout << CYAN << "Press Enter to continue..." << RESET;
            clearInputBuffer();
            return;
        }
        temp = temp->next;
    }
    cout << RED << "Vehicle Not Found!" << RESET << endl;
    cout << CYAN << "Press Enter to continue..." << RESET;
    clearInputBuffer();
}

void binarySearch() {
    if (!isSortedByID) {
        cout << RED << "Error: List must be sorted by ID (use option 5 first)!" << RESET << endl;
        cout << CYAN << "Press Enter to continue..." << RESET;
        clearInputBuffer();
        return;
    }
    int id;
    cout << CYAN << "Enter ID to search: " << RESET;
    while (!(cin >> id) || id <= 0) {
        cout << RED << "Invalid ID. Enter a positive integer: " << RESET;
        clearInputBuffer();
    }

    // Convert linked list to array for binary search
    int count = 0;
    Vehicle* temp = head;
    while (temp) {
        count++;
        temp = temp->next;
    }
    if (count == 0) {
        cout << RED << "No vehicles to search." << RESET << endl;
        cout << CYAN << "Press Enter to continue..." << RESET;
        clearInputBuffer();
        return;
    }

    Vehicle** arr = new Vehicle*[count];
    temp = head;
    for (int i = 0; i < count; i++) {
        arr[i] = temp;
        temp = temp->next;
    }

    // Binary search
    int left = 0, right = count - 1;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (arr[mid]->id == id) {
            cout << GREEN << "Vehicle Found!" << RESET << endl;
            cout << BLUE << "ID: " << RESET << arr[mid]->id << endl;
            cout << BLUE << "Plate Number: " << RESET << arr[mid]->plateNumber << endl;
            cout << BLUE << "Type: " << RESET << arr[mid]->type << endl;
            cout << BLUE << "Duration: " << RESET << fixed << setprecision(2) << arr[mid]->duration << " hrs" << endl;
            cout << BLUE << "Charge: " << RESET << "$" << fixed << setprecision(2) << arr[mid]->charge << endl;
            delete[] arr;
            cout << CYAN << "Press Enter to continue..." << RESET;
            clearInputBuffer();
            return;
        }
        if (arr[mid]->id < id) left = mid + 1;
        else right = mid - 1;
    }
    cout << RED << "Vehicle Not Found!" << RESET << endl;
    delete[] arr;
    cout << CYAN << "Press Enter to continue..." << RESET;
    clearInputBuffer();
}

void deleteVehicle() {
    int id;
    cout << CYAN << "Enter ID to delete: " << RESET;
    while (!(cin >> id) || id <= 0) {
        cout << RED << "Invalid ID. Enter a positive integer: " << RESET;
        clearInputBuffer();
    }
    Vehicle *temp = head, *prev = nullptr;
    while (temp && temp->id != id) {
        prev = temp;
        temp = temp->next;
    }
    if (!temp) {
        cout << RED << "Vehicle not found!" << RESET << endl;
        cout << CYAN << "Press Enter to continue..." << RESET;
        clearInputBuffer();
        return;
    }
    if (prev) prev->next = temp->next;
    else head = temp->next;
    delete temp;
    isSortedByID = false; // Deletion may unsort the list
    cout << GREEN << "Vehicle deleted successfully!" << RESET << endl;
    cout << CYAN << "Press Enter to continue..." << RESET;
    clearInputBuffer();
}

void bubbleSortByID() {
    bool swapped;
    Vehicle *ptr1, *lptr = nullptr;
    if (!head) return;
    do {
        swapped = false;
        ptr1 = head;
        while (ptr1->next != lptr) {
            if (ptr1->id > ptr1->next->id) {
                swap(ptr1->id, ptr1->next->id);
                swap(ptr1->plateNumber, ptr1->next->plateNumber);
                swap(ptr1->type, ptr1->next->type);
                swap(ptr1->charge, ptr1->next->charge);
                swap(ptr1->duration, ptr1->next->duration);
                swapped = true;
            }
            ptr1 = ptr1->next;
        }
        lptr = ptr1;
    } while (swapped);
    isSortedByID = true; // Mark list as sorted by ID
    cout << GREEN << "Sorted by ID using Bubble Sort." << RESET << endl;
    cout << CYAN << "Press Enter to continue..." << RESET;
    clearInputBuffer();
}

void selectionSortByCharge() {
    for (Vehicle* i = head; i && i->next; i = i->next) {
        Vehicle* min = i;
        for (Vehicle* j = i->next; j; j = j->next)
            if (j->charge < min->charge)
                min = j;
        if (min != i) {
            swap(i->id, min->id);
            swap(i->plateNumber, min->plateNumber);
            swap(i->type, min->type);
            swap(i->charge, min->charge);
            swap(i->duration, min->duration);
        }
    }
    isSortedByID = false; // Sorting by charge unsorts ID order
    cout << GREEN << "Sorted by Charge using Selection Sort." << RESET << endl;
    cout << CYAN << "Press Enter to continue..." << RESET;
    clearInputBuffer();
}

void insertionSortByDuration() {
    if (!head || !head->next) return;
    Vehicle* sorted = nullptr;
    while (head) {
        Vehicle* current = head;
        head = head->next;
        if (!sorted || current->duration < sorted->duration) {
            current->next = sorted;
            sorted = current;
        } else {
            Vehicle* temp = sorted;
            while (temp->next && temp->next->duration < current->duration)
                temp = temp->next;
            current->next = temp->next;
            temp->next = current;
        }
    }
    head = sorted;
    isSortedByID = false; // Sorting by duration unsorts ID order
    cout << GREEN << "Sorted by Duration using Insertion Sort." << RESET << endl;
    cout << CYAN << "Press Enter to continue..." << RESET;
    clearInputBuffer();
}

void loadDataFromFile() {
    ifstream file("vehicles.txt");
    if (!file) {
        cout << YELLOW << "No existing data file found. Starting fresh." << RESET << endl;
        cout << CYAN << "Press Enter to continue..." << RESET;
        clearInputBuffer();
        return;
    }
    string line;
    while (getline(file, line)) {
        Vehicle* newVehicle = new Vehicle();
        size_t pos = 0;
        string token;
        int field = 0;
        while ((pos = line.find(',')) != string::npos) {
            token = line.substr(0, pos);
            try {
                if (field == 0) newVehicle->id = stoi(token);
                else if (field == 1) newVehicle->plateNumber = token;
                else if (field == 2) newVehicle->type = token;
                else if (field == 3) newVehicle->duration = stof(token);
                line.erase(0, pos + 1);
                field++;
            } catch (...) {
                cout << YELLOW << "Skipping invalid line in file." << RESET << endl;
                delete newVehicle;
                break;
            }
        }
        if (field == 3) {
            try {
                newVehicle->charge = stof(line);
                if (isUniqueID(newVehicle->id)) {
                    newVehicle->next = head;
                    head = newVehicle;
                } else {
                    cout << YELLOW << "Skipping duplicate ID " << newVehicle->id << " from file." << RESET << endl;
                    delete newVehicle;
                }
            } catch (...) {
                cout << YELLOW << "Skipping invalid line in file." << RESET << endl;
                delete newVehicle;
            }
        }
    }
    file.close();
    isSortedByID = false; // Loaded data may not be sorted
    cout << GREEN << "Data loaded from file successfully!" << RESET << endl;
    cout << CYAN << "Press Enter to continue..." << RESET;
    clearInputBuffer();
}

void saveDataToFile() {
    ofstream file("vehicles.txt");
    Vehicle* temp = head;
    while (temp) {
        file << temp->id << "," << temp->plateNumber << "," << temp->type << ","
             << fixed << setprecision(2) << temp->duration << "," << temp->charge << endl;
        temp = temp->next;
    }
    file.close();
    cout << GREEN << "Data saved to file successfully!" << RESET << endl;
    cout << CYAN << "Press Enter to continue..." << RESET;
    clearInputBuffer();
}

void showDashboard() {
    int total = 0, car = 0, bike = 0, truck = 0;
    float totalCharge = 0, avgCharge = 0;
    Vehicle* temp = head;
    while (temp) {
        total++;
        totalCharge += temp->charge;
        string typeLower = toLower(temp->type);
        if (typeLower == "car") car++;
        else if (typeLower == "bike") bike++;
        else if (typeLower == "truck") truck++;
        temp = temp->next;
    }
    avgCharge = total ? totalCharge / total : 0;
    cout << MAGENTA << "\n==== Dashboard Summary ====" << RESET << endl;
    cout << BOLD << "Total Vehicles: " << RESET << total << endl;
    cout << BOLD << "Total Income: " << RESET << "$" << fixed << setprecision(2) << totalCharge << endl;
    cout << BOLD << "Average Charge: " << RESET << "$" << fixed << setprecision(2) << avgCharge << endl;
    cout << BOLD << "Cars: " << RESET << car << " | " << BOLD << "Bikes: " << RESET << bike << " | " << BOLD << "Trucks: " << RESET << truck << endl;
    cout << CYAN << "Press Enter to continue..." << RESET;
    clearInputBuffer();
}

int main() {
    string user, pass;
    cout << BOLD << "\n==============================\n   Parking Management System\n==============================\n" << RESET;
    cout << "Login\nUsername: ";
    cin >> user;
    cout << "Password: ";
    cin >> pass;
    clearInputBuffer();
    if (user != "admin" || pass != "1234") {
        cout << RED << "Access Denied!" << RESET << endl;
        return 0;
    }

    loadDataFromFile(); // Load data at startup

    int choice;
    do {
        showDashboard();
        cout << "\n==============================\n";
        cout << BOLD << "1. Add Vehicle\n2. Display Vehicles\n3. Linear Search\n4. Binary Search\n5. Sort by ID (Bubble Sort)\n6. Sort by Charge (Selection Sort)\n7. Sort by Duration (Insertion Sort)\n8. Delete Vehicle\n9. Save Data\n10. Exit\nEnter your choice: " << RESET;
        while (!(cin >> choice) || choice < 1 || choice > 10) {
            cout << RED << "Invalid choice. Enter a number between 1 and 10: " << RESET;
            clearInputBuffer();
        }
        switch (choice) {
            case 1: addVehicle(); break;
            case 2: displayVehicles(); break;
            case 3: linearSearch(); break;
            case 4: binarySearch(); break;
            case 5: bubbleSortByID(); break;
            case 6: selectionSortByCharge(); break;
            case 7: insertionSortByDuration(); break;
            case 8: deleteVehicle(); break;
            case 9: saveDataToFile(); break;
            case 10: cout << CYAN << "Exiting..." << RESET << endl; break;
            default: cout << RED << "Invalid choice!" << RESET << endl;
        }
    } while (choice != 10);

    // Free memory
    while (head) {
        Vehicle* temp = head;
        head = head->next;
        delete temp;
    }
    return 0;
}
